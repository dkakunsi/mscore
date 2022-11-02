package com.devit.mscore.workflow;

import static com.devit.mscore.util.Utils.EVENT_TYPE;
import static com.devit.mscore.util.Utils.PRINCIPAL;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.devit.mscore.ApplicationContext;
import com.devit.mscore.Configuration;
import com.devit.mscore.DefaultApplicationContext;
import com.devit.mscore.Event;
import com.devit.mscore.Publisher;
import com.devit.mscore.Registry;
import com.devit.mscore.WorkflowDefinitionRepository;
import com.devit.mscore.WorkflowInstance;
import com.devit.mscore.WorkflowInstanceRepository;
import com.devit.mscore.WorkflowTask;
import com.devit.mscore.WorkflowTaskRepository;
import com.devit.mscore.exception.ApplicationException;
import com.devit.mscore.exception.RegistryException;
import com.devit.mscore.exception.WebClientException;
import com.devit.mscore.workflow.service.WorkflowServiceImpl;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

public class ApplicationTest {

  private ApplicationContext context;

  private Configuration configuration;

  private WorkflowDefinitionRepository definitionRepository;

  private WorkflowInstanceRepository instanceRepository;

  private WorkflowTaskRepository taskRepository;

  private Registry registry;

  private Publisher publisher;

  private EventListener eventListener;

  private String domainChannel = "DOMAIN";

  @Before
  public void setup() throws Exception {
    this.configuration = mock(Configuration.class);
    doReturn("workflow").when(configuration).getServiceName();

    this.publisher = mock(Publisher.class);
    this.definitionRepository = mock(WorkflowDefinitionRepository.class);
    this.instanceRepository = mock(WorkflowInstanceRepository.class);
    this.taskRepository = mock(WorkflowTaskRepository.class);
    this.registry = mock(Registry.class);

    var service = new WorkflowServiceImpl(registry, this.publisher, domainChannel, definitionRepository, instanceRepository,
        taskRepository);

    eventListener = EventListener.of(null, service);
    var contextData = new HashMap<String, Object>();
    contextData.put(EVENT_TYPE, Event.Type.TASK.toString());
    contextData.put(PRINCIPAL, new JSONObject("{\"requestedBy\":\"requestedBy\",\"role\":[\"user\"]}"));
    context = DefaultApplicationContext.of("test", contextData);
  }

  @Test
  public void testNormalFlow_CreateByAction() throws ApplicationException {
    try (MockedStatic<ApplicationContext> utilities = Mockito.mockStatic(ApplicationContext.class)) {
      utilities.when(() -> ApplicationContext.getContext()).thenReturn(context);

      doReturn("definitionId").when(this.registry).get("domain.action");
      var baseUrl = "http://localhost:12000";
      var createInstancePath = "/process/instance/domain.action";
      var expectedResponseBody = new JSONObject("{\"instanceId\":\"workflowId\"}");
      var createdInstance = testCreateInstance(baseUrl, createInstancePath, true, expectedResponseBody);
      testCompleteTask(createdInstance, baseUrl);
    }
  }

  @Test
  public void testNormalFlow_CreateByDefinitionId() throws ApplicationException {
    try (MockedStatic<ApplicationContext> utilities = Mockito.mockStatic(ApplicationContext.class)) {
      utilities.when(() -> ApplicationContext.getContext()).thenReturn(context);

      var baseUrl = "http://localhost:12001";
      var createInstancePath = "/process/definition/definitionId";
      var expectedResponseBody = new JSONObject("{\"instanceId\":\"workflowId\"}");
      var createdInstance = testCreateInstance(baseUrl, createInstancePath, true, expectedResponseBody);
      testCompleteTask(createdInstance, baseUrl);
    }
  }

  private WorkflowInstance testCreateInstance(String baseUrl, String createInstancePath, boolean success, JSONObject expectedResponseBody) throws RegistryException, WebClientException {
    var createdInstance = mock(WorkflowInstance.class);
    var activeTask = mock(WorkflowTask.class);

    // Create instance
    doReturn(createdInstance).when(this.instanceRepository).create(anyString(), anyMap());
    doReturn("workflowId").when(createdInstance).getId();
    doReturn(List.of(activeTask)).when(this.taskRepository).getTasks(anyString());
    doReturn(new JSONObject()).when(createdInstance).toJson(anyList());
    doReturn("definitionId").when(this.registry).get("domain.create");

    var createInstacePayload = "{\"id\":\"entityid\",\"name\":\"name\",\"domain\":\"domain\"}";
    var event = Event.of(Event.Type.CREATE, "domain", new JSONObject(createInstacePayload));
    eventListener.consume(event.toJson());

    if (success) {
      verify(this.publisher, times(1)).publish(anyString(), any(JSONObject.class));
    }
    
    return createdInstance;
  }

  private void testCompleteTask(WorkflowInstance createdInstance, String baseUrl) throws WebClientException {
    var activeTask = mock(WorkflowTask.class);

    doReturn("instanceId").when(activeTask).getInstanceId();
    doReturn(Optional.of(activeTask)).when(this.taskRepository).getTask(anyString());
    doReturn(Optional.of(createdInstance)).when(this.instanceRepository).get(anyString());

    var completeTaskPayload = "{\"taskId\":\"taskId\",\"response\":{\"domain\":\"project\",\"approved\":true}}";
    var event = Event.of(Event.Type.TASK, "domain", new JSONObject(completeTaskPayload));
    eventListener.consume(event.toJson());

    verify(this.publisher, times(2)).publish(anyString(), any(JSONObject.class));
  }
}
