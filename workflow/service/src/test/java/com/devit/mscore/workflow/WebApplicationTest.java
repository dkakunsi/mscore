package com.devit.mscore.workflow;

import static com.devit.mscore.util.Constants.ACTION;
import static com.devit.mscore.util.Constants.ENTITY;
import static com.devit.mscore.util.Constants.EVENT_TYPE;
import static com.devit.mscore.util.Constants.PRINCIPAL;
import static com.devit.mscore.util.Constants.TASK;
import static com.devit.mscore.util.Constants.VARIABLE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.devit.mscore.Configuration;
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
import com.devit.mscore.workflow.api.ApiFactory;
import com.devit.mscore.workflow.service.WorkflowServiceImpl;

import java.util.List;
import java.util.Optional;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import kong.unirest.Unirest;

public class WebApplicationTest {

  private static final String REQUEST_ACTION = "domain.action";

  private ApiFactory apiFactory;

  private Configuration configuration;

  private WorkflowDefinitionRepository definitionRepository;

  private WorkflowInstanceRepository instanceRepository;

  private WorkflowTaskRepository taskRepository;

  private Registry registry;

  private Publisher publisher;

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

    var service = new WorkflowServiceImpl(registry, this.publisher, domainChannel, definitionRepository,
        instanceRepository,
        taskRepository);

    this.apiFactory = ApiFactory.of(this.configuration, null);
    this.apiFactory.addService(service);
  }

  @Test
  public void givenValidRequest_WhenExecuteWorkflowIsCalledAndErrorRetrievingDefinition_ThenShouldSuccess()
      throws ApplicationException {
    doReturn(Optional.of("12001")).when(this.configuration).getConfig("services.workflow.web.port");
    doThrow(RegistryException.class).when(this.registry).get(REQUEST_ACTION);

    var uri = "http://localhost:12001/process";
    var server = this.apiFactory.server();

    server.start();
    testWithoutCreatingInstance(uri);
    server.stop();
  }

  @Test
  public void givenValidRequest_WhenExecuteWorkflowIsCalledAndGetNullWhenRetrievingDefinition_ThenShouldSuccess()
      throws ApplicationException {
    doReturn(Optional.of("12002")).when(this.configuration).getConfig("services.workflow.web.port");
    doReturn(null).when(this.registry).get(REQUEST_ACTION);

    var uri = "http://localhost:12002/process";
    var server = this.apiFactory.server();

    server.start();
    testWithoutCreatingInstance(uri);
    server.stop();
  }

  @Test
  public void givenValidRequest_WhenExecuteWorkflowIsCalledAndGetEmptyStringWhenRetrievingDefinition_ThenShouldSuccess()
      throws ApplicationException {
    doReturn(Optional.of("12003")).when(this.configuration).getConfig("services.workflow.web.port");
    doReturn("").when(this.registry).get(REQUEST_ACTION);

    var uri = "http://localhost:12003/process";
    var server = this.apiFactory.server();

    server.start();
    testWithoutCreatingInstance(uri);
    server.stop();
  }

  private void testWithoutCreatingInstance(String uri) throws RegistryException, WebClientException {
    var entity = "{\"id\":\"entityid\",\"name\":\"name\",\"domain\":\"domain\"}";
    var variables = "{\"var1\":\"val1\"}";
    var requestPayload = new JSONObject();
    requestPayload.put(ENTITY, new JSONObject(entity));
    requestPayload.put(VARIABLE, new JSONObject(variables));
    var event = Event.of(Event.Type.CREATE, "domain", REQUEST_ACTION, requestPayload);

    var serverResponse = Unirest.post(uri)
        .header(ACTION, REQUEST_ACTION)
        .body(event.toJson().toString())
        .asString();

    assertThat(serverResponse.isSuccess(), is(true));
    var serverResponseBody = new JSONObject(serverResponse.getBody());
    var expectedResponseBody = "{\"instanceId\":\"UNKNOWN\"}";
    assertEquals(expectedResponseBody, serverResponseBody.toString());

    verify(this.publisher).publish(anyString(), any(JSONObject.class));
  }

  @Test
  public void givenValidRequest_WhenExecuteWorkflowIsCalled_ThenShouldSuccess() throws ApplicationException {
    doReturn(Optional.of("12000")).when(this.configuration).getConfig("services.workflow.web.port");
    doReturn("definitionId").when(this.registry).get(REQUEST_ACTION);

    var processUri = "http://localhost:12000/process";
    var taskUri = "http://localhost:12000/task";
    var expectedResponseBody = new JSONObject("{\"instanceId\":\"workflowId\"}");
    var server = this.apiFactory.server();

    server.start();
    var createdInstance = testCreateInstance(processUri, true, expectedResponseBody);
    testCompleteTask(createdInstance, taskUri);
    server.stop();
  }

  private WorkflowInstance testCreateInstance(String uri, boolean success,
      JSONObject expectedResponseBody) throws RegistryException, WebClientException {
    var createdInstance = mock(WorkflowInstance.class);
    var activeTask = mock(WorkflowTask.class);

    // Create instance
    doReturn(createdInstance).when(this.instanceRepository).create(anyString(), anyMap());
    doReturn("workflowId").when(createdInstance).getId();
    doReturn(List.of(activeTask)).when(this.taskRepository).getTasks(anyString());
    doReturn(new JSONObject()).when(createdInstance).toJson(anyList());
    doReturn("definitionId").when(this.registry).get(REQUEST_ACTION);

    var entity = "{\"id\":\"entityid\",\"name\":\"name\",\"domain\":\"domain\"}";
    var requestPayload = new JSONObject();
    requestPayload.put(ENTITY, new JSONObject(entity));
    requestPayload.put(VARIABLE, new JSONObject("{\"var1\":\"val1\"}"));
    var event = Event.of(Event.Type.CREATE, "domain", REQUEST_ACTION, requestPayload);

    var serverResponse = Unirest.post(uri)
        .header(ACTION, REQUEST_ACTION)
        .body(event.toJson().toString())
        .asString();

    assertThat(serverResponse.isSuccess(), is(success));
    var serverResponseBody = new JSONObject(serverResponse.getBody());
    assertEquals(expectedResponseBody.toString(), serverResponseBody.toString());

    if (success) {
      verify(this.publisher, times(1)).publish(anyString(), any(JSONObject.class));
    }

    return createdInstance;
  }

  private void testCompleteTask(WorkflowInstance createdInstance, String uri) throws WebClientException {
    var activeTask = mock(WorkflowTask.class);

    doReturn("instanceId").when(activeTask).getInstanceId();
    doReturn(Optional.of(activeTask)).when(this.taskRepository).getTask(anyString());
    doReturn(Optional.of(createdInstance)).when(this.instanceRepository).get(anyString());

    var completeTaskVariable = new JSONObject("{\"domain\":\"project\",\"approved\":true}");
    var completeTaskEntity = new JSONObject("{\"id\":\"taskId\"}");
    var event = Event.of(Event.Type.UPDATE, TASK, REQUEST_ACTION, completeTaskEntity, completeTaskVariable);

    var serverResponse = Unirest.post(uri)
        .header(EVENT_TYPE, Event.Type.UPDATE.toString())
        .header(ACTION, REQUEST_ACTION)
        .header(PRINCIPAL, "{}")
        .body(event.toJson().toString())
        .asString();

    assertThat(serverResponse.isSuccess(), is(true));
    verify(this.publisher, times(2)).publish(anyString(), any(JSONObject.class));
  }
}
