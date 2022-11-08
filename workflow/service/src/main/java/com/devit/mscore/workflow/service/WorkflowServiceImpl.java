package com.devit.mscore.workflow.service;

import static com.devit.mscore.ApplicationContext.getContext;
import static com.devit.mscore.util.AttributeConstants.DOMAIN;
import static com.devit.mscore.util.AttributeConstants.getId;
import static com.devit.mscore.util.AttributeConstants.getName;
import static com.devit.mscore.util.Utils.ACTION;
import static com.devit.mscore.util.Utils.BREADCRUMB_ID;
import static com.devit.mscore.util.Utils.EVENT_TYPE;
import static com.devit.mscore.util.Utils.PRINCIPAL;

import com.devit.mscore.Event;
import com.devit.mscore.Logger;
import com.devit.mscore.Publisher;
import com.devit.mscore.Registry;
import com.devit.mscore.Resource;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.json.JSONObject;

public class WorkflowServiceImpl implements WorkflowService {

  public static final String WORKFLOW = "workflow";

  private static final Logger LOGGER = ApplicationLogger.getLogger(WorkflowServiceImpl.class);

  private static final String FAIL_REGISTER_MESSAGE_TEMPLATE = "Failed to register workflow '%s'. Definition ID is not found";

  private Registry registry;

  private Publisher publisher;

  private String domainChannel;

  private WorkflowDefinitionRepository definitionRepository;

  private WorkflowInstanceRepository instanceRepository;

  private WorkflowTaskRepository taskRepository;

  public WorkflowServiceImpl(Registry registry, Publisher publisher, String domainChannel,
      WorkflowDefinitionRepository definitionRepository, WorkflowInstanceRepository instanceRepository,
      WorkflowTaskRepository taskRepository) throws ApplicationException {
    this.registry = registry;
    this.publisher = publisher;
    this.definitionRepository = definitionRepository;
    this.instanceRepository = instanceRepository;
    this.taskRepository = taskRepository;
    this.domainChannel = domainChannel;
  }

  @Override
  public void deployDefinition(WorkflowDefinition definition) throws ProcessException {
    LOGGER.info("Deploying definition: dilayanan{}", definition);

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
    var definitionId = this.definitionRepository.getDefinitionId(definition.getResourceName())
        .orElseThrow(() -> new RegistryException(String.format(FAIL_REGISTER_MESSAGE_TEMPLATE, definition.getName())));
    this.registry.add(definition.getName(), definitionId);
    LOGGER.info("Workflow '{}' is added to registry", definition.getName());
  }

  @Override
  public WorkflowInstance executeWorkflow(String action, JSONObject entity, Map<String, Object> variables)
      throws ProcessException {
    try {
      var definitionId = this.registry.get(action);
      var instance = this.instanceRepository.create(definitionId, populateVariables(entity, variables));
      syncCreate(instance);
      return instance;
    } catch (RegistryException ex) {
      throw new ProcessException(String.format("Process definition is not found for action '%s'", action), ex);
    }
  }

  private Map<String, Object> populateVariables(JSONObject entity, Map<String, Object> variables) {
    final Map<String, Object> vars;
    if (variables == null) {
      vars = new HashMap<>();
    } else {
      vars = variables;
    }

    var context = getContext();
    vars.put("entity", entity.toString());
    vars.put(DOMAIN, AttributeConstants.getDomain(entity));
    vars.put("businessKey", getId(entity));
    vars.put("name", getName(entity));
    vars.put("createdBy", context.getRequestedBy());

    vars.put(BREADCRUMB_ID, context.getBreadcrumbId());
    context.getEventType().ifPresent(et -> vars.put(EVENT_TYPE, et));
    context.getPrincipal().ifPresent(p -> vars.put(PRINCIPAL, p.toString()));
    context.getAction().ifPresent(a -> vars.put(ACTION, a));

    return vars;
  }

  private void syncCreate(WorkflowInstance instance) {
    var tasks = this.taskRepository.getTasks(instance.getId());
    sync(instance, Event.Type.CREATE, tasks);
  }

  private void syncUpdate(WorkflowInstance instance, WorkflowTask completedTask) {
    var activeTasks = this.taskRepository.getTasks(instance.getId());
    var tasks = new ArrayList<>(activeTasks);
    tasks.add(completedTask);
    sync(instance, Event.Type.UPDATE, tasks);
  }

  private void sync(WorkflowInstance instance, Event.Type eventType, List<WorkflowTask> tasks) {
    var context = getContext();
    var jsonData = instance.toJson(tasks);
    var event = Event.of(eventType, WORKFLOW, context.getAction().orElse(null), jsonData);
    publisher.publish(domainChannel, event.toJson());
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
  public void completeTask(String taskId, Map<String, Object> variables) throws ProcessException {
    var task = this.taskRepository.getTask(taskId)
        .orElseThrow(() -> new ProcessException(String.format("No task found for id: %s", taskId)));

    // load instance prior to task completion
    // for completed instance, get will return null.
    var instanceId = task.getInstanceId();
    var instance = this.instanceRepository.get(instanceId)
        .orElseThrow(() -> new ProcessException(String.format("No instance found for id: %s", instanceId)));

    final Map<String, Object> vars;
    if (variables == null) {
      vars = new HashMap<>();
    } else {
      vars = variables;
    }
    var context = getContext();
    vars.put(BREADCRUMB_ID, context.getBreadcrumbId());
    context.getEventType().ifPresent(et -> vars.put(EVENT_TYPE, et));
    context.getPrincipal().ifPresent(p -> vars.put(PRINCIPAL, p.toString()));
    context.getAction().ifPresent(a -> vars.put(ACTION, a));

    this.taskRepository.complete(taskId, variables);
    task.complete();

    if (this.instanceRepository.isCompleted(instanceId)) {
      instance.complete();
    }
    syncUpdate(instance, task);
  }

  @Override
  public String getDomain() {
    return WorkflowDefinition.PROCESS;
  }

  @Override
  public Resource getSchema() {
    throw new UnsupportedOperationException();
  }
}
