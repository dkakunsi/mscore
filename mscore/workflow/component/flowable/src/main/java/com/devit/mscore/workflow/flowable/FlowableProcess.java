package com.devit.mscore.workflow.flowable;

import static com.devit.mscore.util.AttributeConstants.getId;
import static com.devit.mscore.util.AttributeConstants.getName;
import static com.devit.mscore.web.WebUtils.INFORMATION;
import static com.devit.mscore.web.WebUtils.SUCCESS;
import static com.devit.mscore.workflow.flowable.FlowableDefinition.PROCESS;
import static com.devit.mscore.workflow.flowable.FlowableDefinition.WORKFLOW;
import static org.flowable.engine.ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE;

import com.devit.mscore.ApplicationContext;
import com.devit.mscore.Registry;
import com.devit.mscore.Service;
import com.devit.mscore.WorkflowProcess;
import com.devit.mscore.WorkflowDefinition;
import com.devit.mscore.WorkflowObject;
import com.devit.mscore.exception.ProcessException;
import com.devit.mscore.exception.RegistryException;
import com.devit.mscore.exception.WebClientException;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlowableProcess implements WorkflowProcess, Service {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlowableProcess.class);

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
    public void start() {
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
    public void deployDefinition(ApplicationContext context, WorkflowDefinition workflowDefinition)
            throws ProcessException {
        LOGGER.info("BreadcrumbId: {}. Deploying definition: {}", context.getBreadcrumbId(), workflowDefinition);

        try {
            if (isDefinitionExists(workflowDefinition)) {
                // since the registry is in memory, we need to populate the existing definition
                registerWorkflow(context, workflowDefinition);
                LOGGER.info("BreadcrumbId: {}. Definition {} is not deployed. It is exists.", context.getBreadcrumbId(),
                        workflowDefinition.getResourceName());
                return;
            }
    
            this.repositoryService.createDeployment()
                    .addString(workflowDefinition.getResourceName(), workflowDefinition.getContent()).deploy();

            registerWorkflow(context, workflowDefinition);
            LOGGER.debug("BreadcrumbId: {}. Definition {} is deployed.", context.getBreadcrumbId(), workflowDefinition);
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
    private void registerWorkflow(ApplicationContext context, WorkflowDefinition workflowDefinition)
            throws RegistryException {
        var processDefinition = getProcessDefinition(workflowDefinition.getResourceName());
        if (processDefinition == null) {
            LOGGER.warn("BreadcrumbId: {}. Workflof did not registered: No process definition was found.", context.getBreadcrumbId());
            return;
        }
        var workflowRegister = workflowDefinition.getMessage(processDefinition.getId());
        var workflowRegisterName = getName(workflowRegister);
        this.registry.add(context, workflowRegisterName, workflowRegister.toString());

        LOGGER.info("Workflow {} is added to registry. {}", workflowDefinition, workflowRegister);
    }

    private ProcessDefinition getProcessDefinition(String resourceName) {
        var definitions = this.repositoryService.createProcessDefinitionQuery()
                .processDefinitionResourceName(resourceName).list();

        return (definitions == null || definitions.isEmpty()) ? null : definitions.get(0);
    }

    @Override
    public FlowableProcessInstance createInstanceByAction(ApplicationContext context, String action, JSONObject entity,
            Map<String, Object> variables) throws ProcessException {

        try {
            var definition = this.registry.get(context, action);
            return createInstance(context, new JSONObject(definition).getString(WORKFLOW), entity, variables);
        } catch (RegistryException ex) {
            LOGGER.error("BreadcrumbId: {}. Cannot create instance. Definition is not found for action '{}'",
                    context.getBreadcrumbId(), action);
            throw new ProcessException(String.format("Process definition is not found for action '%s'", action), ex);
        }
    }

    @Override
    public FlowableProcessInstance createInstance(ApplicationContext context, String processDefinitionId,
            JSONObject entity, Map<String, Object> variables) throws ProcessException {

        variables.put("entity", entity.toString());
        variables.put("domain", AttributeConstants.getDomain(entity));
        variables.put("businessKey", getId(entity));
        variables.put("name", getName(entity));
        variables.put("createdBy", context.getRequestedBy());

        return createInstance(context, processDefinitionId, variables);
    }

    public synchronized FlowableProcessInstance createInstance(ApplicationContext context, String processDefinitionId,
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

            var flowableInstance = new FlowableProcessInstance(this.runtimeService, processInstance);
            syncProcessInstance(context, flowableInstance);
            syncCreatedTask(context, processInstance.getProcessInstanceId());
            return flowableInstance;
        } finally {
            // nothing
        }
    }

    @Override
    public FlowableProcessInstance getInstance(ApplicationContext context, String processInstanceId) {
        var processInstance = this.runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId)
                .singleResult();

        return (processInstance == null) ? null : new FlowableProcessInstance(this.runtimeService, processInstance);
    }

    HistoricProcessInstance getHistoricInstance(String processInstanceId) {
        return this.historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId)
                .singleResult();
    }

    @Override
    public List<WorkflowObject> getTasks(ApplicationContext context, String processInstanceId) {
        var tasks = this.taskService.createTaskQuery().processInstanceId(processInstanceId).list();
        return (tasks == null) ? null : tasks.stream().map(task -> new FlowableTask(task, this.historyService)).collect(Collectors.toList());
    }

    @Override
    public void completeTask(ApplicationContext context, String taskId, JSONObject taskResponse)
            throws ProcessException {
        var task = getTask(taskId);
        if (task == null) {
            LOGGER.warn("No task found with id: {}", taskId);
            return;
        }

        var processInstance = getInstance(task.getProcessInstanceId());
        taskResponse.put("breadcrumbId", context.getBreadcrumbId());
        this.taskService.complete(taskId, taskResponse.toMap());

        syncCompletedTask(context, taskId);
        syncCreatedTask(context, task.getProcessInstanceId());
        syncCompletableProcessInstance(context, processInstance);
    }

    private FlowableTask getTask(String taskId) {
        var task = this.taskService.createTaskQuery().taskId(taskId).singleResult();
        return task == null ? null : new FlowableTask(task, this.historyService);
    }

    private FlowableProcessInstance getInstance(String processInstanceId) {
        var processInstance = this.runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
        return new FlowableProcessInstance(this.runtimeService, processInstance);
    }

    private void syncCompletableProcessInstance(ApplicationContext context, FlowableProcessInstance flowableInstance) {
        var historicProcessInstance = this.historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(flowableInstance.getProcessInstanceId()).finished().singleResult();

        if (historicProcessInstance == null) {
            return;
        }

        flowableInstance.complete();
        syncProcessInstance(context, flowableInstance);
    }

    private void syncProcessInstance(ApplicationContext context, FlowableProcessInstance flowableInstance) {
        var client = this.dataClient.getClient();
        var uri = this.dataClient.getWorkflowUri(context);

        var instanceJson = flowableInstance.toJson();
        LOGGER.debug("BreadcrumbdId: {}. Synchronizing workflow instance: {}", context.getBreadcrumbId(), instanceJson);
        try {
            var response = client.post(context, uri, Optional.of(instanceJson));
            var responseType = WebUtils.getMessageType(response.getInt("code"));
            if (!StringUtils.equalsAny(responseType, INFORMATION, SUCCESS)) {
                LOGGER.error("BreadcrumbId: {}. Cannot synchronize workflow instance: {}.", context.getBreadcrumbId(), response.getJSONObject("payload"));
            }
        } catch (WebClientException ex) {
            LOGGER.error("BreadcrumbId: {}. Cannot synchronize workflow instance: {}.", context.getBreadcrumbId(), flowableInstance);
        }
    }

    private void syncCreatedTask(ApplicationContext context, String processInstanceId) {
        var tasks = this.taskService.createTaskQuery().processInstanceId(processInstanceId).list();
        if (tasks == null || tasks.isEmpty()) {
            return;
        }

        var flowableTask = new FlowableTask(tasks.get(0), this.historyService);
        syncTask(context, flowableTask);
    }

    private void syncCompletedTask(ApplicationContext context, String taskId) {
        var historicTaskInstance = this.historyService.createHistoricTaskInstanceQuery().taskId(taskId).singleResult();
        var flowableTask = new FlowableTask(historicTaskInstance, this.historyService);
        flowableTask.complete();
        syncTask(context, flowableTask);
    }

    private void syncTask(ApplicationContext context, FlowableTask flowableTask) {
        var client = this.dataClient.getClient();
        var uri = this.dataClient.getTaskUri(context);

        var taskJson = flowableTask.toJson();
        LOGGER.debug("BreadcrumbdId: {}. Synchronizing workflow task: {}", context.getBreadcrumbId(), taskJson);
        try {
            var response = client.post(context, uri, Optional.of(taskJson));
            var responseType = WebUtils.getMessageType(response.getInt("code"));
            if (!StringUtils.equalsAny(responseType, INFORMATION, SUCCESS)) {
                LOGGER.error("BreadcrumbId: {}. Cannot synchronize workflow task: {}.", context.getBreadcrumbId(), response.getJSONObject("payload"));
            }
        } catch (WebClientException ex) {
            LOGGER.error("BreadcrumbId: {}. Cannot synchronize workflow task: {}.", context.getBreadcrumbId(), flowableTask);
        }
    }
}
