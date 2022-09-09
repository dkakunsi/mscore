package com.devit.mscore.workflow.service;

import static com.devit.mscore.ApplicationContext.getContext;
import static com.devit.mscore.util.AttributeConstants.getId;
import static com.devit.mscore.util.AttributeConstants.getName;
import static com.devit.mscore.util.Utils.BREADCRUMB_ID;
import static com.devit.mscore.web.WebUtils.INFORMATION;
import static com.devit.mscore.web.WebUtils.SUCCESS;

import com.devit.mscore.DataClient;
import com.devit.mscore.Logger;
import com.devit.mscore.Registry;
import com.devit.mscore.WorkflowDefinition;
import com.devit.mscore.WorkflowDefinitionRepository;
import com.devit.mscore.WorkflowInstance;
import com.devit.mscore.WorkflowInstanceRepository;
import com.devit.mscore.WorkflowService;
import com.devit.mscore.WorkflowTask;
import com.devit.mscore.WorkflowTaskRepository;
import com.devit.mscore.exception.ApplicationException;
import com.devit.mscore.exception.ProcessException;
import com.devit.mscore.exception.RegistryException;
import com.devit.mscore.exception.WebClientException;
import com.devit.mscore.logging.ApplicationLogger;
import com.devit.mscore.util.AttributeConstants;
import com.devit.mscore.web.WebUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.json.JSONObject;

import liquibase.repackaged.org.apache.commons.lang3.StringUtils;

public class WorkflowServiceImpl implements WorkflowService {

  public static final String WORKFLOW = "workflow";

  private static final Logger LOGGER = ApplicationLogger.getLogger(WorkflowServiceImpl.class);

  private Registry registry;

  private DataClient dataClient;

  private WorkflowDefinitionRepository definitionRepository;

  private WorkflowInstanceRepository instanceRepository;

  private WorkflowTaskRepository taskRepository;

  public WorkflowServiceImpl(Registry registry, DataClient dataClient, WorkflowDefinitionRepository definitionRepository,
      WorkflowInstanceRepository instanceRepository, WorkflowTaskRepository taskRepository)
      throws ApplicationException {
    try {
      this.registry = (Registry) registry.clone();
      this.dataClient = dataClient;
      this.definitionRepository = definitionRepository;
      this.instanceRepository = instanceRepository;
      this.taskRepository = taskRepository;
    } catch (CloneNotSupportedException ex) {
      throw new ApplicationException(ex);
    }
  }

  @Override
  public void deployDefinition(WorkflowDefinition definition) throws ProcessException {
    LOGGER.info("Deploying definition: {}", definition);

    try {
      if (!this.definitionRepository.isExists(definition)) {
        this.definitionRepository.deploy(definition);
      }
      registerWorkflow(definition);
    } catch (RegistryException ex) {
      throw new ProcessException("Cannot register process deployment.", ex);
    } catch (Exception ex) {
      throw new ProcessException("Definition deployment failed.", ex);
    }
  }

  @SuppressWarnings("PMD.GuardLogStatement")
  private void registerWorkflow(WorkflowDefinition definition) throws RegistryException {
    var definitionId = this.definitionRepository.getDefinitionId(definition.getResourceName());
    if (definitionId.isEmpty()) {
      var message = String.format("Failed to register workflow '%s'. Definition ID is not found", definition.getName());
      throw new RegistryException(message);
    }
    this.registry.add(definition.getName(), definitionId.get());
    LOGGER.info("Workflow {} is added to registry.", definition.getName());
  }

  @Override
  public WorkflowInstance createInstance(String definitionId, JSONObject entity, Map<String, Object> variables)
      throws ProcessException {
    var instance = this.instanceRepository.create(definitionId, populateVariables(entity, variables));
    sync(instance);
    return instance;
  }

  private Map<String, Object> populateVariables(JSONObject entity, Map<String, Object> variables) {
    var context = getContext();
    variables.put("entity", entity.toString());
    variables.put("domain", AttributeConstants.getDomain(entity));
    variables.put("businessKey", getId(entity));
    variables.put("name", getName(entity));
    variables.put("createdBy", context.getRequestedBy());
    return variables;
  }

  @SuppressWarnings("PMD.GuardLogStatement")
  private void sync(WorkflowInstance instance) {
    var tasks = this.taskRepository.getTasks(instance.getId());
    var json = instance.toJson(tasks);

    try {
      var client = this.dataClient.getClient();
      var uri = this.dataClient.getWorkflowUri();

      var response = client.post(uri, Optional.of(json));
      var responseType = WebUtils.getMessageType(response.getInt("code"));
      if (!StringUtils.equalsAny(responseType, INFORMATION, SUCCESS)) {
        LOGGER.error("Cannot synchronize workflow instance: {}.", response.getString("payload"));
      }
    } catch (WebClientException ex) {
      LOGGER.error("Cannot synchronize workflow instance: {}.", ex, instance);
    }
  }

  @Override
  public WorkflowInstance createInstanceByAction(String action, JSONObject entity, Map<String, Object> variables)
      throws ProcessException {
    try {
      var definitionId = this.registry.get(action);
      return createInstance(definitionId, entity, variables);
    } catch (RegistryException ex) {
      throw new ProcessException(String.format("Process definition is not found for action '%s'", action), ex);
    }
  }

  @Override
  public Optional<WorkflowInstance> getInstance(String instanceId) {
    return this.instanceRepository.get(instanceId);
  }

  @Override
  public List<WorkflowTask> getTasks(String instanceId) {
    return this.taskRepository.getTasks(instanceId);
  }

  @Override
  public void completeTask(String taskId, JSONObject taskResponse) throws ProcessException {
    var taskOpt = this.taskRepository.getTask(taskId);
    if (taskOpt.isEmpty()) {
      throw new ProcessException(String.format("No task found for id: %s", taskId));
    }
    var task = taskOpt.get();

    // load instance prior to task completion
    // for completed instance, get will return null.
    var instanceId = task.getInstanceId();
    var instanceOpt = this.instanceRepository.get(instanceId);
    if (instanceOpt.isEmpty()) {
      throw new ProcessException(String.format("No instance found for id: %s", instanceId));
    }
    var instance = instanceOpt.get();

    taskResponse.put(BREADCRUMB_ID, getContext().getBreadcrumbId());
    this.taskRepository.complete(taskId, taskResponse.toMap());

    if (this.instanceRepository.isCompleted(instanceId)) {
      instance.complete();
    }
    sync(instance);
  }

  @Override
  public String getDomain() {
    return WorkflowDefinition.PROCESS;
  }

  @Override
  public Object clone() throws CloneNotSupportedException {
    return super.clone();
  }
}
