package com.devit.mscore.workflow.service;

import static com.devit.mscore.ApplicationContext.getContext;
import static com.devit.mscore.util.AttributeConstants.CREATED_BY;
import static com.devit.mscore.util.AttributeConstants.NAME;
import static com.devit.mscore.util.AttributeConstants.getId;
import static com.devit.mscore.util.AttributeConstants.getName;
import static com.devit.mscore.util.Constants.ACTION;
import static com.devit.mscore.util.Constants.BREADCRUMB_ID;
import static com.devit.mscore.util.Constants.DOMAIN;
import static com.devit.mscore.util.Constants.ENTITY;
import static com.devit.mscore.util.Constants.EVENT_TYPE;
import static com.devit.mscore.util.Constants.PRINCIPAL;
import static com.devit.mscore.util.Constants.PROCESS;

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

import org.apache.commons.lang3.StringUtils;
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
    LOGGER.info("Deploying definition: {}", definition);

    try {
      if (!definitionRepository.isExists(definition)) {
        definitionRepository.deploy(definition);
      }
      registerWorkflow(definition);
    } catch (RegistryException ex) {
      throw new ProcessException("Cannot register process deployment", ex);
    } catch (Exception ex) {
      throw new ProcessException("Definition deployment failed", ex);
    }
  }

  private void registerWorkflow(WorkflowDefinition definition) throws RegistryException {
    var definitionId = definitionRepository.getDefinitionId(definition.getResourceName())
        .orElseThrow(() -> new RegistryException(String.format(FAIL_REGISTER_MESSAGE_TEMPLATE, definition.getName())));
    registry.add(definition.getName(), definitionId);
    LOGGER.info("Workflow '{}' for definition '{}' is added to registry", definition.getName(), definitionId);
  }

  @Override
  public Optional<WorkflowInstance> executeWorkflow(String action, JSONObject entity, Map<String, Object> variables)
      throws ProcessException {
    String definitionId;
    try {
      definitionId = registry.get(action);
    } catch (RegistryException ex) {
      LOGGER.error("Error when retrieving definition for action '{}': {}", ex, action, ex.getMessage());
      return createDomainWithoutWorkflow(entity, action);
    }

    if (StringUtils.isBlank(definitionId)) {
      LOGGER.info("Process definition is not found for action '{}'", action);
      return createDomainWithoutWorkflow(entity, action);
    }

    var processVariables = populateVariables(entity, variables);
    var instance = instanceRepository.create(definitionId, processVariables);
    syncCreate(instance);
    return Optional.of(instance);
  }

  private Optional<WorkflowInstance> createDomainWithoutWorkflow(JSONObject entity, String action) {
    LOGGER.info("Create domain object without executing workflow for action '%s'", action);
    var domain = AttributeConstants.getDomain(entity);
    var eventType = getContext().getEventType().get();
    publishDomainEvent(entity, domain, Event.Type.valueOf(eventType.toUpperCase()));
    return Optional.empty();
  }

  private Map<String, Object> populateVariables(JSONObject entity, Map<String, Object> variables) {
    final Map<String, Object> vars = variables != null ? variables : new HashMap<>();
    var context = getContext();
    vars.put(ENTITY, entity.toString());
    vars.put(DOMAIN, AttributeConstants.getDomain(entity));
    vars.put("businessKey", getId(entity));
    vars.put(NAME, getName(entity));

    vars.put(BREADCRUMB_ID, context.getBreadcrumbId());
    vars.put(CREATED_BY, context.getRequestedBy());
    context.getEventType().ifPresent(et -> vars.put(EVENT_TYPE, et));
    context.getPrincipal().ifPresent(p -> vars.put(PRINCIPAL, p.toString()));
    context.getAction().ifPresent(a -> vars.put(ACTION, a));

    return vars;
  }

  private void syncCreate(WorkflowInstance instance) {
    var tasks = taskRepository.getTasks(instance.getId());

    var jsonData = instance.toJson(tasks);
    publishDomainEvent(jsonData, WORKFLOW, Event.Type.CREATE);
  }

  private void syncUpdate(WorkflowInstance instance, WorkflowTask completedTask) {
    var activeTasks = taskRepository.getTasks(instance.getId());
    var tasks = new ArrayList<>(activeTasks);
    tasks.add(completedTask);

    var jsonData = instance.toJson(tasks);
    publishDomainEvent(jsonData, WORKFLOW, Event.Type.UPDATE);
  }

  private void publishDomainEvent(JSONObject entity, String domain, Event.Type eventType) {
    var action = getContext().getAction().get();
    var event = Event.of(eventType, domain, action, entity);
    publisher.publish(domainChannel, event.toJson());
  }

  @Override
  public Optional<WorkflowInstance> getInstance(String instanceId) {
    return instanceRepository.get(instanceId);
  }

  @Override
  public List<WorkflowTask> getTasks(String instanceId) {
    return taskRepository.getTasks(instanceId);
  }

  @Override
  public void completeTask(String taskId, Map<String, Object> variables) throws ProcessException {
    var task = taskRepository.getTask(taskId)
        .orElseThrow(() -> new ProcessException(String.format("No task found for id: %s", taskId)));

    // load instance prior to task completion
    // for completed instance, get will return null.
    var instanceId = task.getInstanceId();
    var instance = instanceRepository.get(instanceId)
        .orElseThrow(() -> new ProcessException(String.format("No instance found for id: %s", instanceId)));

    final Map<String, Object> vars = variables != null ? variables : new HashMap<>();
    var context = getContext();
    vars.put(BREADCRUMB_ID, context.getBreadcrumbId());
    context.getEventType().ifPresent(et -> vars.put(EVENT_TYPE, et));
    context.getPrincipal().ifPresent(p -> vars.put(PRINCIPAL, p.toString()));
    context.getAction().ifPresent(a -> vars.put(ACTION, a));

    taskRepository.complete(taskId, variables);
    task.complete();

    if (instanceRepository.isCompleted(instanceId)) {
      instance.complete();
    }
    syncUpdate(instance, task);
  }

  @Override
  public String getDomain() {
    return PROCESS;
  }

  @Override
  public Resource getSchema() {
    throw new UnsupportedOperationException();
  }
}
