package com.devit.mscore.workflow.service;

import static com.devit.mscore.ApplicationContext.getContext;
import static com.devit.mscore.util.AttributeConstants.getId;
import static com.devit.mscore.util.AttributeConstants.getName;
import static com.devit.mscore.util.Utils.BREADCRUMB_ID;

import com.devit.mscore.Event;
import com.devit.mscore.Logger;
import com.devit.mscore.Publisher;
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
import com.devit.mscore.logging.ApplicationLogger;
import com.devit.mscore.util.AttributeConstants;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.json.JSONObject;

public class WorkflowServiceImpl implements WorkflowService {

  public static final String WORKFLOW = "workflow";

  private static final Logger LOGGER = ApplicationLogger.getLogger(WorkflowServiceImpl.class);

  private Registry registry;

  private Publisher publisher;

  private WorkflowDefinitionRepository definitionRepository;

  private WorkflowInstanceRepository instanceRepository;

  private WorkflowTaskRepository taskRepository;

  public WorkflowServiceImpl(Registry registry, Publisher publisher, WorkflowDefinitionRepository definitionRepository,
      WorkflowInstanceRepository instanceRepository, WorkflowTaskRepository taskRepository)
      throws ApplicationException {
    try {
      this.registry = (Registry) registry.clone();
      this.publisher = publisher;
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
      throw new ProcessException("Cannot register process deployment", ex);
    } catch (Exception ex) {
      throw new ProcessException("Definition deployment failed", ex);
    }
  }

  private void registerWorkflow(WorkflowDefinition definition) throws RegistryException {
    var definitionId = this.definitionRepository.getDefinitionId(definition.getResourceName());
    if (definitionId.isEmpty()) {
      var message = String.format("Failed to register workflow '%s'. Definition ID is not found", definition.getName());
      throw new RegistryException(message);
    }
    this.registry.add(definition.getName(), definitionId.get());
    LOGGER.info("Workflow {} is added to registry", definition.getName());
  }

  @Override
  public WorkflowInstance createInstance(String definitionId, JSONObject entity, Map<String, Object> variables)
      throws ProcessException {
    var instance = this.instanceRepository.create(definitionId, populateVariables(entity, variables));
    syncCreate(instance);
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

  private void syncCreate(WorkflowInstance instance) {
    sync(instance, Event.Type.CREATE);
  }

  private void syncUpdate(WorkflowInstance instance) {
    sync(instance, Event.Type.UPDATE);
  }

  private void sync(WorkflowInstance instance, Event.Type eventType) {
    var tasks = this.taskRepository.getTasks(instance.getId());
    var jsonData = instance.toJson(tasks);
    var event = Event.of(eventType, WORKFLOW, jsonData);
    var message = event.toJson();
    publisher.publish(message);
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
    syncUpdate(instance);
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
