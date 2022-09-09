package com.devit.mscore.workflow;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.devit.mscore.Configuration;
import com.devit.mscore.DataClient;
import com.devit.mscore.Registry;
import com.devit.mscore.Service;
import com.devit.mscore.WorkflowDefinitionRepository;
import com.devit.mscore.WorkflowInstance;
import com.devit.mscore.WorkflowInstanceRepository;
import com.devit.mscore.WorkflowTask;
import com.devit.mscore.WorkflowTaskRepository;
import com.devit.mscore.exception.ApplicationException;
import com.devit.mscore.exception.RegistryException;
import com.devit.mscore.exception.WebClientException;
import com.devit.mscore.web.Client;
import com.devit.mscore.workflow.api.ApiFactory;
import com.devit.mscore.workflow.service.WorkflowServiceImpl;

import java.util.List;
import java.util.Optional;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import kong.unirest.Unirest;

public class ApplicationTest {

  private ApiFactory apiFactory;

  private Configuration configuration;

  private WorkflowDefinitionRepository definitionRepository;

  private WorkflowInstanceRepository instanceRepository;

  private WorkflowTaskRepository taskRepository;

  private Registry registry;

  private Client client;

  @Before
  public void setup() throws Exception {
    this.configuration = mock(Configuration.class);
    doReturn("workflow").when(configuration).getServiceName();

    this.definitionRepository = mock(WorkflowDefinitionRepository.class);
    this.instanceRepository = mock(WorkflowInstanceRepository.class);
    this.taskRepository = mock(WorkflowTaskRepository.class);
    this.registry = mock(Registry.class);
    doReturn(this.registry).when(registry).clone();

    this.client = mock(Client.class);
    var dataClient = mock(DataClient.class);
    doReturn(this.client).when(dataClient).getClient();
    doReturn("http://data/workflow").when(dataClient).getWorkflowUri();

    var service = new WorkflowServiceImpl(registry, dataClient, definitionRepository, instanceRepository,
        taskRepository);

    this.apiFactory = ApiFactory.of(this.configuration, null);
    this.apiFactory.addService(service);
  }

  @Test
  public void testNormalFlow_CreateByAction() throws ApplicationException {
    doReturn(Optional.of("12000")).when(this.configuration).getConfig("services.workflow.web.port");

    var server = this.apiFactory.server();
    server.start();

    doReturn("definitionId").when(this.registry).get("domain.action");
    var baseUrl = "http://localhost:12000";
    var createInstancePath = "/process/instance/domain.action";
    var expectedResponseBody = new JSONObject("{\"instanceId\":\"workflowId\"}");
    var createdInstance = testCreateInstance(baseUrl, createInstancePath, null, true, expectedResponseBody);
    testCompleteTask(createdInstance, baseUrl);

    server.stop();
  }

  @Test
  public void testNormalFlow_CreateByDefinitionId() throws ApplicationException {
    doReturn(Optional.of("12001")).when(this.configuration).getConfig("services.workflow.web.port");

    var server = this.apiFactory.server();
    server.start();

    var baseUrl = "http://localhost:12001";
    var createInstancePath = "/process/definition/definitionId";
    var expectedResponseBody = new JSONObject("{\"instanceId\":\"workflowId\"}");
    var createdInstance = testCreateInstance(baseUrl, createInstancePath, null, true, expectedResponseBody);
    testCompleteTask(createdInstance, baseUrl);

    server.stop();
  }

  @Test
  public void testCreateInstance_WithFailureSync() throws Exception {
    doReturn(Optional.of("12002")).when(this.configuration).getConfig("services.workflow.web.port");

    var server = this.apiFactory.server();
    server.start();

    var baseUrl = "http://localhost:12002";
    var createInstancePath = "/process/definition/definitionId";
    var dataServiceResponse = new JSONObject("{\"code\":400,\"payload\":\"bad request\"}");
    var expectedResponseBody = new JSONObject("{\"instanceId\":\"workflowId\"}");
    testCreateInstance(baseUrl, createInstancePath, dataServiceResponse, true, expectedResponseBody);

    server.stop();
  }

  @Test
  public void testCreateInstance_WithEmptyService_ShouldFail() throws ApplicationException {
    doReturn(Optional.of("12003")).when(this.configuration).getConfig("services.workflow.web.port");
    var mockedService = mock(Service.class);
    doReturn("process").when(mockedService).getDomain();

    var internalApiFactory = ApiFactory.of(this.configuration, null);
    internalApiFactory.addService(mockedService);
    var server = internalApiFactory.server();
    server.start();

    var baseUrl = "http://localhost:12003";
    var createInstancePath = "/process/definition/definitionId";
    var expectedResponseBody = new JSONObject("{\"message\":\"No workflow process is registered.\",\"type\":\"SERVER ERROR\"}");
    testCreateInstance(baseUrl, createInstancePath, null, false, expectedResponseBody);

    server.stop();
  }

  private WorkflowInstance testCreateInstance(String baseUrl, String createInstancePath, JSONObject dataServiceResponse, boolean success, JSONObject expectedResponseBody) throws RegistryException, WebClientException {
    var createdInstance = mock(WorkflowInstance.class);
    var activeTask = mock(WorkflowTask.class);

    // Create instance
    doReturn(createdInstance).when(this.instanceRepository).create(anyString(), anyMap());
    doReturn("workflowId").when(createdInstance).getId();
    doReturn(List.of(activeTask)).when(this.taskRepository).getTasks(anyString());
    doReturn(new JSONObject()).when(createdInstance).toJson(anyList());
    doReturn("definitionId").when(this.registry).get("action");

    if (dataServiceResponse == null) {
      dataServiceResponse = new JSONObject("{\"code\":200,\"payload\":\"success\"}");
    }
    doReturn(dataServiceResponse).when(this.client).post(anyString(), any());

    var createInstacePayload = "{\"id\":\"entityid\",\"name\":\"name\",\"domain\":\"domain\"}";
    var createInstanceUrl = baseUrl + createInstancePath;
    var serverResponse = Unirest.post(createInstanceUrl).body(createInstacePayload).asString();
    assertThat(serverResponse.isSuccess(), is(success));
    var serverResponseBody = new JSONObject(serverResponse.getBody());
    assertTrue(serverResponseBody.similar(expectedResponseBody));

    if (success) {
      verify(client, times(1)).post(eq("http://data/workflow"), any());
    }

    return createdInstance;
  }

  private void testCompleteTask(WorkflowInstance createdInstance, String baseUrl) throws WebClientException {
    var activeTask = mock(WorkflowTask.class);

    doReturn("instanceId").when(activeTask).getInstanceId();
    doReturn(Optional.of(activeTask)).when(this.taskRepository).getTask(anyString());
    doReturn(Optional.of(createdInstance)).when(this.instanceRepository).get(anyString());

    var completeTaskUrl = baseUrl + "/process/task/taskId";
    var completeTaskPayload = "{\"domain\":\"project\",\"approved\":true}";
    var serverResponse = Unirest.put(completeTaskUrl).body(completeTaskPayload).asString();
    assertTrue(serverResponse.isSuccess());
    verify(client, times(2)).post(eq("http://data/workflow"), any());
  }
}