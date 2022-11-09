package com.devit.mscore.workflow;

import static com.devit.mscore.util.Constants.ACTION;
import static com.devit.mscore.util.Constants.EVENT_TYPE;
import static com.devit.mscore.util.Constants.PRINCIPAL;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
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
  public void givenValidRequest_WhenExecuteWorkflowIsCalled_ShouldSuccess() throws ApplicationException {
    doReturn(Optional.of("12000")).when(this.configuration).getConfig("services.workflow.web.port");

    var server = this.apiFactory.server();
    server.start();

    doReturn("definitionId").when(this.registry).get(REQUEST_ACTION);
    var baseUrl = "http://localhost:12000";
    var createInstancePath = "/process";
    var expectedResponseBody = new JSONObject("{\"instanceId\":\"workflowId\"}");
    var createdInstance = testCreateInstance(baseUrl, createInstancePath, true, expectedResponseBody);
    testCompleteTask(createdInstance, baseUrl);

    server.stop();
  }

  private WorkflowInstance testCreateInstance(String baseUrl, String createInstancePath, boolean success,
      JSONObject expectedResponseBody) throws RegistryException, WebClientException {
    var createdInstance = mock(WorkflowInstance.class);
    var activeTask = mock(WorkflowTask.class);

    // Create instance
    doReturn(createdInstance).when(this.instanceRepository).create(anyString(), anyMap());
    doReturn("workflowId").when(createdInstance).getId();
    doReturn(List.of(activeTask)).when(this.taskRepository).getTasks(anyString());
    doReturn(new JSONObject()).when(createdInstance).toJson(anyList());
    doReturn("definitionId").when(this.registry).get(REQUEST_ACTION);

    var createInstancePayload = "{\"id\":\"entityid\",\"name\":\"name\",\"domain\":\"domain\"}";
    var data = new JSONObject();
    data.put("entity", new JSONObject(createInstancePayload));
    data.put("variable", new JSONObject("{\"var1\":\"val1\"}"));
    var event = Event.of(Event.Type.CREATE, "domain", REQUEST_ACTION, data);
    var serverResponse = Unirest.post(baseUrl + createInstancePath)
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

  private void testCompleteTask(WorkflowInstance createdInstance, String baseUrl) throws WebClientException {
    var activeTask = mock(WorkflowTask.class);

    doReturn("instanceId").when(activeTask).getInstanceId();
    doReturn(Optional.of(activeTask)).when(this.taskRepository).getTask(anyString());
    doReturn(Optional.of(createdInstance)).when(this.instanceRepository).get(anyString());

    var completeTaskUrl = baseUrl + "/task/taskId";
    var completeTaskPayload = "{\"domain\":\"project\",\"approved\":true}";
    var event = Event.of(Event.Type.UPDATE, "domain", REQUEST_ACTION, new JSONObject(completeTaskPayload));
    var serverResponse = Unirest.put(completeTaskUrl)
        .header(EVENT_TYPE, Event.Type.UPDATE.toString())
        .header(PRINCIPAL, "{}")
        .body(event.toJson().toString())
        .asString();
    assertThat(serverResponse.isSuccess(), is(true));
    verify(this.publisher, times(2)).publish(anyString(), any(JSONObject.class));
  }
}
