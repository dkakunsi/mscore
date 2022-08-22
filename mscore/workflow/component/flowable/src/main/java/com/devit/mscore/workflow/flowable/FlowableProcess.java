package com.devit.mscore.workflow.flowable;

import static com.devit.mscore.ApplicationContext.getContext;
import static com.devit.mscore.util.AttributeConstants.getId;
import static com.devit.mscore.util.AttributeConstants.getName;
import static com.devit.mscore.web.WebUtils.INFORMATION;
import static com.devit.mscore.web.WebUtils.SUCCESS;
import static com.devit.mscore.workflow.flowable.FlowableDefinition.PROCESS;
import static com.devit.mscore.workflow.flowable.FlowableDefinition.WORKFLOW;
import static org.flowable.common.engine.impl.AbstractEngineConfiguration.DB_SCHEMA_UPDATE_TRUE;

import com.devit.mscore.Logger;
import com.devit.mscore.Registry;
import com.devit.mscore.Service;
import com.devit.mscore.WorkflowDefinition;
import com.devit.mscore.WorkflowObject;
import com.devit.mscore.WorkflowProcess;
import com.devit.mscore.exception.ProcessException;
import com.devit.mscore.exception.RegistryException;
import com.devit.mscore.exception.WebClientException;
import com.devit.mscore.logging.ApplicationLogger;
import com.devit.mscore.util.AttributeConstants;
import com.devit.mscore.web.WebUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.flowable.engine.HistoryService;
import org.flowable.engine.IdentityService;
import org.flowable.engine.ProcessEngineConfiguration;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.flowable.engine.repository.ProcessDefinition;
import org.json.JSONObject;

public class FlowableProcess implements WorkflowProcess, Service {

  private static final Logger LOGGER = ApplicationLogger.getLogger(FlowableProcess.class);

  private final DataSource dataSource;

  private final Registry registry;

  private final DataClient dataClient;

  private RepositoryService repositoryService;

  private RuntimeService runtimeService;

  private HistoryService historyService;

  private TaskService taskService;

  private IdentityService identityService;

  FlowableProcess(DataSource dataSource, Registry registry, DataClient dataClient) {
    this.dataSource = dataSource;
    this.registry = registry;
    this.dataClient = dataClient;
  }

  @Override
  public String getDomain() {
    return PROCESS;
  }

  @Override
  public synchronized void start() {
    // @formatter:off
    ProcessEngineConfiguration processEngineConfiguration = new StandaloneProcessEngineConfiguration()
        .setDataSource(this.dataSource)
        .setDatabaseSchemaUpdate(DB_SCHEMA_UPDATE_TRUE);
    // @formatter:on

    var processEngine = processEngineConfiguration.buildProcessEngine();
    this.repositoryService = processEngine.getRepositoryService();
    this.runtimeService = processEngine.getRuntimeService();
    this.taskService = processEngine.getTaskService();
    this.historyService = processEngine.getHistoryService();
    this.identityService = processEngine.getIdentityService();

    LOGGER.info("Flowable engine is started.");
  }

  @Override
  public void stop() {
    // no need to stop Flowable process.
  }

  @Override
  @SuppressWarnings("PMD.GuardLogStatement")
  public void deployDefinition(WorkflowDefinition workflowDefinition)
      throws ProcessException {
    LOGGER.info("Deploying definition: {}", workflowDefinition);

    try {
      if (isDefinitionExists(workflowDefinition)) {
        // since the registry is in memory, we need to populate the existing definition
        registerWorkflow(workflowDefinition);
        LOGGER.info("Definition {} is not deployed. It is exists.",
            workflowDefinition.getResourceName());
        return;
      }

      this.repositoryService.createDeployment()
          .addString(workflowDefinition.getResourceName(), workflowDefinition.getContent()).deploy();

      registerWorkflow(workflowDefinition);
      LOGGER.debug("Definition {} is deployed.", workflowDefinition);
    } catch (RegistryException ex) {
      throw new ProcessException("Cannot register process deployment.", ex);
    } catch (Exception ex) {
      throw new ProcessException("Definition deployment failed.", ex);
    }
  }

  private boolean isDefinitionExists(WorkflowDefinition workflowDefinition) {
    return getProcessDefinition(workflowDefinition.getResourceName()) != null;
  }

  /**
   * Register the deployed definition id and add to the registry.
   *
   * @param context            of the request
   * @param workflowDefinition to update
   * @throws RegistryException error when updating registry
   */
  private void registerWorkflow(WorkflowDefinition workflowDefinition)
      throws RegistryException {
    var processDefinition = getProcessDefinition(workflowDefinition.getResourceName());
    if (processDefinition == null) {
      LOGGER.warn("Workflof did not registered: No process definition was found.");
      return;
    }
    var workflowRegister = workflowDefinition.getMessage(processDefinition.getId());
    var workflowRegisterName = getName(workflowRegister);
    this.registry.add(workflowRegisterName, workflowRegister.toString());

    LOGGER.info("Workflow {} is added to registry. {}", workflowDefinition, workflowRegister);
  }

  private ProcessDefinition getProcessDefinition(String resourceName) {
    var definitions = this.repositoryService.createProcessDefinitionQuery()
        .processDefinitionResourceName(resourceName).list();

    return (definitions == null || definitions.isEmpty()) ? null : definitions.get(0);
  }

  @Override
  public FlowableProcessInstance createInstanceByAction(String action, JSONObject entity,
      Map<String, Object> variables) throws ProcessException {

    try {
      var definition = this.registry.get(action);
      return createInstance(new JSONObject(definition).getString(WORKFLOW), entity, variables);
    } catch (RegistryException ex) {
      LOGGER.error("Cannot create instance. Definition is not found for action '{}'", action);
      throw new ProcessException(String.format("Process definition is not found for action '%s'", action), ex);
    }
  }

  @Override
  public FlowableProcessInstance createInstance(String processDefinitionId,
      JSONObject entity, Map<String, Object> variables) throws ProcessException {

    var context = getContext();
    variables.put("entity", entity.toString());
    variables.put("domain", AttributeConstants.getDomain(entity));
    variables.put("businessKey", getId(entity));
    variables.put("name", getName(entity));
    variables.put("createdBy", context.getRequestedBy());

    return createInstance(processDefinitionId, variables);
  }

  public synchronized FlowableProcessInstance createInstance(String processDefinitionId,
      Map<String, Object> variables) {

    try {
      var createdBy = variables.get("createdBy").toString();
      this.identityService.setAuthenticatedUserId(createdBy);

      var businessKey = variables.get("businessKey").toString();
      var name = variables.get("name").toString();

      // @formatter:off
            var processInstance = this.runtimeService.createProcessInstanceBuilder()
                    .processDefinitionId(processDefinitionId)
                    .businessKey(businessKey)
                    .name(name)
                    .variables(variables)
                    .start();
            // @formatter:on

      if (processInstance == null) {
        LOGGER.warn("No process started for processDefinitionId: {}", processDefinitionId);
        return null;
      }

      var processInstanceVariables = this.runtimeService.getVariables(processInstance.getId());
      var flowableInstance = new FlowableProcessInstance(processInstance, processInstanceVariables);
      syncProcessInstance(flowableInstance);
      syncCreatedTask(processInstance.getProcessInstanceId());
      return flowableInstance;
    } finally {
      // nothing
    }
  }

  @Override
  public FlowableProcessInstance getInstance(String processInstanceId) {
    var processInstance = this.runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId)
        .singleResult();

    if (processInstance == null) {
      return null;
    }

    var processInstanceVariables = this.runtimeService.getVariables(processInstance.getId());
    return new FlowableProcessInstance(processInstance, processInstanceVariables);
  }

  HistoricProcessInstance getHistoricInstance(String processInstanceId) {
    return this.historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId)
        .singleResult();
  }

  @Override
  public List<WorkflowObject> getTasks(String processInstanceId) {
    var tasks = this.taskService.createTaskQuery().processInstanceId(processInstanceId).list();
    if (tasks == null) {
      return null;
    }

    return tasks.stream().map(task -> {
      var taskVariables = getTaskVariables(task.getProcessInstanceId());
      return new FlowableTask(task, taskVariables);
    }).collect(Collectors.toList());
  }

  private Map<String, Object> getTaskVariables(String processInstanceId) {
    var historicProcessInstance = this.historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
    return historicProcessInstance.getProcessVariables();
  }

  @Override
  public void completeTask(String taskId, JSONObject taskResponse)
      throws ProcessException {
    var task = getTask(taskId);
    if (task == null) {
      LOGGER.warn("No task found with id: {}", taskId);
      return;
    }

    var processInstance = getInstanceById(task.getProcessInstanceId());

    var context = getContext();
    taskResponse.put("breadcrumbId", context.getBreadcrumbId());
    this.taskService.complete(taskId, taskResponse.toMap());

    syncCompletedTask(taskId);
    syncCreatedTask(task.getProcessInstanceId());
    syncCompletableProcessInstance(processInstance);
  }

  private FlowableTask getTask(String taskId) {
    var task = this.taskService.createTaskQuery().taskId(taskId).singleResult();
    if (task == null) {
      return null;
    }

    var taskVariables = getTaskVariables(task.getProcessInstanceId());
    return new FlowableTask(task, taskVariables);
  }

  public FlowableProcessInstance getInstanceById(String processInstanceId) {
    var processInstance = this.runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId)
        .singleResult();

    var processInstanceVariables = this.runtimeService.getVariables(processInstance.getId());
    return new FlowableProcessInstance(processInstance, processInstanceVariables);
  }

  private void syncCompletableProcessInstance(FlowableProcessInstance flowableInstance) {
    var historicProcessInstance = this.historyService.createHistoricProcessInstanceQuery()
        .processInstanceId(flowableInstance.getProcessInstanceId()).finished().singleResult();

    if (historicProcessInstance == null) {
      return;
    }

    flowableInstance.complete();
    syncProcessInstance(flowableInstance);
  }

  @SuppressWarnings("PMD.GuardLogStatement")
  private void syncProcessInstance(FlowableProcessInstance flowableInstance) {
    var client = this.dataClient.getClient();
    var uri = this.dataClient.getWorkflowUri();

    var instanceJson = flowableInstance.toJson();
    LOGGER.debug("Synchronizing workflow instance: {}", instanceJson);
    try {
      var response = client.post(uri, Optional.of(instanceJson));
      var responseType = WebUtils.getMessageType(response.getInt("code"));
      if (!StringUtils.equalsAny(responseType, INFORMATION, SUCCESS)) {
        LOGGER.error("Cannot synchronize workflow instance: {}.", response.getJSONObject("payload"));
      }
    } catch (WebClientException ex) {
      LOGGER.error("Cannot synchronize workflow instance: {}.", flowableInstance);
    }
  }

  private void syncCreatedTask(String processInstanceId) {
    var tasks = this.taskService.createTaskQuery().processInstanceId(processInstanceId).list();
    if (tasks == null || tasks.isEmpty()) {
      return;
    }

    var task = tasks.get(0);
    var taskVariables = getTaskVariables(task.getProcessInstanceId());
    var flowableTask = new FlowableTask(task, taskVariables);
    syncTask(flowableTask);
  }

  private void syncCompletedTask(String taskId) {
    var historicTaskInstance = this.historyService.createHistoricTaskInstanceQuery().taskId(taskId).singleResult();
    if (historicTaskInstance == null) {
      return;
    }

    var taskVariables = getTaskVariables(historicTaskInstance.getProcessInstanceId());
    var flowableTask = new FlowableTask(historicTaskInstance, taskVariables);
    flowableTask.complete();
    syncTask(flowableTask);
  }

  @SuppressWarnings("PMD.GuardLogStatement")
  private void syncTask(FlowableTask flowableTask) {
    var client = this.dataClient.getClient();
    var uri = this.dataClient.getTaskUri();

    var taskJson = flowableTask.toJson();
    LOGGER.debug("Synchronizing workflow task: {}", taskJson);
    try {
      var response = client.post(uri, Optional.of(taskJson));
      var responseType = WebUtils.getMessageType(response.getInt("code"));
      if (!StringUtils.equalsAny(responseType, INFORMATION, SUCCESS)) {
        LOGGER.error("Cannot synchronize workflow task: {}.", response.getJSONObject("payload"));
      }
    } catch (WebClientException ex) {
      LOGGER.error("Cannot synchronize workflow task: {}.", flowableTask);
    }
  }

  @Override
  public Object clone() throws CloneNotSupportedException {
    return super.clone();
  }
}
