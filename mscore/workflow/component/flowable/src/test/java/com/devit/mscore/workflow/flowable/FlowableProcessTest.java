package com.devit.mscore.workflow.flowable;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.devit.mscore.ApplicationContext;
import com.devit.mscore.Configuration;
import com.devit.mscore.DefaultApplicationContext;
import com.devit.mscore.Registry;
import com.devit.mscore.ServiceRegistration;
import com.devit.mscore.WorkflowObject;
import com.devit.mscore.exception.ConfigException;
import com.devit.mscore.exception.ProcessException;
import com.devit.mscore.exception.RegistryException;
import com.devit.mscore.exception.WebClientException;
import com.devit.mscore.web.Client;
import com.devit.mscore.workflow.flowable.delegate.TestDelegate;

import java.io.File;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import io.zonky.test.db.postgres.junit.EmbeddedPostgresRules;
import io.zonky.test.db.postgres.junit.SingleInstancePostgresRule;

public class FlowableProcessTest {

  private static final String TASK_INDEX = "workflowtask";

  private static final String INSTANCE_INDEX = "workflow";

  private static final String VARIABLES = "{\"assignee\":\"assignee\",\"approver\":\"approver\",\"createdBy\":\"createdBy\",\"owner\":\"owner\",\"businessKey\":\"entityid\",\"name\":\"name\",\"domain\":\"domain\"}";

  private static final String ENTITY = "{\"id\":\"entityid\",\"name\":\"name\",\"domain\":\"domain\"}";

  @Rule
  public SingleInstancePostgresRule pgRule = EmbeddedPostgresRules.singleInstance();

  private Configuration configuration;

  private Registry registry;

  private DataClient dataClient;

  private ServiceRegistration serviceRegistration;

  private Client client;

  private FlowableProcess process;

  private ApplicationContext context;

  @Before
  public void setup() throws RegistryException, ConfigException, CloneNotSupportedException {
    this.configuration = mock(Configuration.class);
    doReturn(Optional.of("localhost")).when(this.configuration).getConfig("process.db.host");
    doReturn(Optional.of("5432")).when(this.configuration).getConfig("process.db.port");
    doReturn(Optional.of("flowable")).when(this.configuration).getConfig("process.db.name");
    doReturn(Optional.of("process")).when(this.configuration).getConfig("process.db.schema");
    doReturn(Optional.of("postgres")).when(this.configuration).getConfig("process.db.username");
    doReturn(Optional.of("postgres")).when(this.configuration).getConfig("process.db.password");
    doReturn(Optional.of(TASK_INDEX)).when(this.configuration).getConfig("process.index.task");
    doReturn(Optional.of(INSTANCE_INDEX)).when(this.configuration).getConfig("process.index.instance");
    doReturn(Optional.of("definition")).when(this.configuration).getConfig("process.definition.location");
    doReturn(true).when(this.configuration).has("workflow.definition.location");

    this.serviceRegistration = mock(ServiceRegistration.class);
    doReturn(this.serviceRegistration).when(this.serviceRegistration).clone();
    doReturn("http://data/domain").when(this.serviceRegistration).get(anyString());
    doReturn("http://data/workflow").when(this.serviceRegistration).get("workflow");
    doReturn("http://data/task").when(this.serviceRegistration).get("task");

    this.client = mock(Client.class);
    doReturn(this.client).when(this.client).clone();

    this.dataClient = new DataClient(this.client, this.serviceRegistration, "workflow", "task");

    this.registry = mock(Registry.class);

    // This will be used to verify that TestHandler is executed.
    TestDelegate.registry = this.registry;

    this.process = getProcess();
    var map = new HashMap<String, Object>();
    map.put("principal", new JSONObject("{\"requestedBy\":\"createdBy\"}"));
    this.context = DefaultApplicationContext.of("test", map);

    // only for coverage
    this.dataClient.getDomainUri("domain");
    this.process.getDomain();
    this.process.stop();
  }

  private FlowableProcess getProcess() {
    var dataSource = this.pgRule.getEmbeddedPostgres().getPostgresDatabase();
    var manager = FlowableWorkflowFactory.of(this.configuration, this.registry);
    return (FlowableProcess) manager.workflowProcess(dataSource, this.dataClient);
  }

  private File getResource(String resourceName) throws URISyntaxException {
    var resource = getClass().getClassLoader().getResource(resourceName);
    return new File(resource.toURI());
  }

  @Test
  public void testFlowableProcess_FailedDeployment() throws Exception {
    // Start process engine
    this.process.start();

    // Deploy definition
    var definition = getResource("invalid.definition.1.bpmn20.xml");
    var flowableDefinition = new FlowableDefinition(definition);

    var ex = assertThrows(ProcessException.class, () -> this.process.deployDefinition(flowableDefinition));
    assertThat(ex.getMessage(), is("Definition deployment failed."));
  }

  @Test
  public void testFlowableProcess_RegistryFailure() throws Exception {
    doThrow(new RegistryException("message")).when(this.registry).add(anyString(), anyString());

    // Start process engine
    this.process.start();

    // Deploy definition
    var definition = getResource("definition/domain.action.1.bpmn20.xml");
    var flowableDefinition = new FlowableDefinition(definition);

    var ex = assertThrows(ProcessException.class, () -> this.process.deployDefinition(flowableDefinition));
    assertThat(ex.getMessage(), is("Cannot register process deployment."));
  }

  @Test
  public void testFlowableProcess_WebClientException_OnInstanceCreation() throws Exception {
    doThrow(new WebClientException("message")).when(this.client).post(anyString(), any());
    var argumentCaptor = ArgumentCaptor.forClass(String.class);

    // Start process engine
    this.process.start();

    // Deploy definition
    var definition = getResource("definition/domain.action.1.bpmn20.xml");
    var flowableDefinition = new FlowableDefinition(definition);
    this.process.deployDefinition(flowableDefinition);

    // Verify that deployed definition is added to registry
    verify(this.registry, times(1)).add(anyString(), argumentCaptor.capture());
    var argumentObject = new JSONObject(argumentCaptor.getValue());
    assertThat(argumentObject.length(), is(4));
    assertThat(argumentObject.getString("name"), is("domain.action"));
    assertThat(argumentObject.getString("resourceName"), is("domain.action.1.bpmn20.xml"));
    assertTrue(StringUtils.isNotBlank(argumentObject.getString("workflow")));
    assertTrue(StringUtils.isNotBlank(argumentObject.getString("content")));

    // Create instance
    this.process.createInstance(argumentObject.getString("workflow"), new JSONObject(VARIABLES).toMap());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testFlowableProcess_WebClientException_OnTaskCompletion() throws Exception {
    try (MockedStatic<ApplicationContext> utilities = Mockito.mockStatic(ApplicationContext.class)) {
      utilities.when(() -> ApplicationContext.getContext())
          .thenReturn(this.context);

      var response = new JSONObject("{\"code\":200,\"payload\":\"success\"}");
      when(this.client.post(anyString(), any())).thenReturn(response)
          .thenThrow(new WebClientException("message"));
      var resourceArgumentCaptor = ArgumentCaptor.forClass(String.class);
      var taskArgumentCaptor = ArgumentCaptor.forClass(Optional.class);
      var instanceArgumentCaptor = ArgumentCaptor.forClass(Optional.class);

      // Start process engine
      this.process.start();

      // Deploy definition
      var definition = getResource("definition/domain.action.1.bpmn20.xml");
      var flowableDefinition = new FlowableDefinition(definition);
      this.process.deployDefinition(flowableDefinition);

      // Verify that deployed definition is added to registry
      verify(this.registry, times(1)).add(anyString(), resourceArgumentCaptor.capture());
      var argumentObject = new JSONObject(resourceArgumentCaptor.getValue());
      assertThat(argumentObject.length(), is(4));
      assertThat(argumentObject.getString("name"), is("domain.action"));
      assertThat(argumentObject.getString("resourceName"), is("domain.action.1.bpmn20.xml"));
      assertTrue(StringUtils.isNotBlank(argumentObject.getString("workflow")));
      assertTrue(StringUtils.isNotBlank(argumentObject.getString("content")));

      // Create instance
      var processInstance = this.process.createInstance(argumentObject.getString("workflow"),
          new JSONObject(VARIABLES).toMap());
      assertNotNull(processInstance);

      // Verify instance is indexed
      verify(this.client, times(1)).post(eq("http://data/workflow"), instanceArgumentCaptor.capture());
      argumentObject = (JSONObject) instanceArgumentCaptor.getValue().get();
      assertFalse(argumentObject.isEmpty());
      assertTrue(StringUtils.isNotBlank(argumentObject.getString("id")));
      assertThat(argumentObject.getJSONObject("entity").getString("id"), is("entityid"));
      assertThat(argumentObject.getString("name"), is("name"));
      assertThat(argumentObject.getString("createdBy"), is("createdBy"));
      assertThat(argumentObject.getString("status"), is("Active"));

      // verify task is indexed
      verify(this.client, times(1)).post(eq("http://data/task"), taskArgumentCaptor.capture());
      argumentObject = (JSONObject) taskArgumentCaptor.getValue().get();
      assertFalse(argumentObject.isEmpty());
      assertTrue(StringUtils.isNotBlank(argumentObject.getString("id")));
      assertThat(argumentObject.getString("name"), is("Test Approve"));
      assertThat(argumentObject.getString("assignee"), is("approver"));
      assertThat(argumentObject.getString("status"), is("Active"));

      var refProcessInstance = argumentObject.getJSONObject("processInstance");
      assertThat(refProcessInstance.getString("domain"), is("workflow"));
      assertThat(refProcessInstance.getString("id"), is(processInstance.getId()));

      // Get tasks
      var tasks = this.process.getTasks(processInstance.getId());
      assertThat(tasks.size(), is(1));
      var task = tasks.get(0);
      assertThat(task.getName(), is("Test Approve"));

      // Complete task and get next task
      var taskResponse = "{\"approved\":true}";
      this.process.completeTask(task.getId(), new JSONObject(taskResponse));
    }
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testFlowableProcess() throws Exception {
    try (MockedStatic<ApplicationContext> utilities = Mockito.mockStatic(ApplicationContext.class)) {
      utilities.when(() -> ApplicationContext.getContext())
          .thenReturn(this.context);

      var response = new JSONObject("{\"code\":200,\"payload\":\"success\"}");
      when(this.client.post(anyString(), any()))
          .thenReturn(response)
          .thenReturn(response)
          .thenThrow(new WebClientException("message"));

      var registryArgumentCaptor = ArgumentCaptor.forClass(String.class);
      var taskArgumentCaptor = ArgumentCaptor.forClass(Optional.class);
      var instanceArgumentCaptor = ArgumentCaptor.forClass(Optional.class);

      // Start process engine
      this.process.start();

      // Deploy definition
      var definition = getResource("definition/domain.action.1.bpmn20.xml");
      var flowableDefinition = new FlowableDefinition(definition);
      this.process.deployDefinition(flowableDefinition);
      verify(this.registry, times(1)).add(anyString(), registryArgumentCaptor.capture());
      var argumentObject = new JSONObject(registryArgumentCaptor.getValue());
      verifyDeployedDefinitionIsAddedToRegistry(argumentObject);

      doReturn(argumentObject.toString()).when(this.registry).get("domain.action");

      // Create instance
      var processInstance = this.process.createInstanceByAction(argumentObject.getString("name"),
          new JSONObject(ENTITY), new JSONObject(VARIABLES).toMap());
      assertNotNull(processInstance);

      verify(this.client, times(1)).post(eq("http://data/workflow"), instanceArgumentCaptor.capture());
      argumentObject = (JSONObject) instanceArgumentCaptor.getValue().get();
      verifyProcessInstanceIndexIsUpdated(argumentObject, "Active");

      verify(this.client, times(1)).post(eq("http://data/task"), taskArgumentCaptor.capture());
      argumentObject = (JSONObject) taskArgumentCaptor.getValue().get();
      var task = verifyTaskIsCreatedAndIndexed(processInstance, argumentObject, "Test Approve", "approver", "Active");

      // Complete task and get next task
      var taskResponse = "{\"approved\":true}";
      this.process.completeTask(task.getId(), new JSONObject(taskResponse));

      verify(this.client, times(3)).post(eq("http://data/task"), taskArgumentCaptor.capture());
      var arguments = taskArgumentCaptor.getAllValues();
      assertThat(arguments.size(), is(4));

      argumentObject = (JSONObject) arguments.get(2).get();
      verifyTaskIndexIsUpdated(processInstance, argumentObject, "Test Approve", "approver", "Complete");

      argumentObject = (JSONObject) arguments.get(3).get();
      task = verifyTaskIsCreatedAndIndexed(processInstance, argumentObject, "Test Approved", "assignee", "Active");

      // Verify that Java Delegate is called
      // verify(this.repository, times(1)).hashCode();

      // Complete final task
      this.process.completeTask(task.getId(), new JSONObject());

      // Verify current index is updated
      verify(this.client, times(4)).post(eq("http://data/task"), taskArgumentCaptor.capture());
      argumentObject = (JSONObject) taskArgumentCaptor.getValue().get();
      verifyTaskIndexIsUpdated(processInstance, argumentObject, "Test Approved", "assignee", "Complete");

      // Verify all task is completed
      var tasks = this.process.getTasks(processInstance.getId());
      assertThat(tasks.size(), is(0));

      // Verify the process instance is completed
      var processInstanceId = processInstance.getId();
      processInstance = this.process.getInstance(processInstanceId);
      assertNull(processInstance);

      // Verify process instance is moved to history
      var historicProcessInstance = this.process.getHistoricInstance(processInstanceId);
      assertNotNull(historicProcessInstance);

      // Verify instance index is updated
      verify(this.client, times(2)).post(eq("http://data/workflow"), instanceArgumentCaptor.capture());
      argumentObject = (JSONObject) instanceArgumentCaptor.getValue().get();
      verifyProcessInstanceIndexIsUpdated(argumentObject, "Complete");
    }
  }

  private WorkflowObject verifyTaskIsCreatedAndIndexed(FlowableProcessInstance processInstance,
      JSONObject argumentObject, String name, String assignee, String status) throws WebClientException {
    var tasks = this.process.getTasks(processInstance.getId());
    assertThat(tasks.size(), is(1));
    var task = tasks.get(0);
    assertThat(task.getName(), is(name));

    verifyTaskIndexIsUpdated(processInstance, argumentObject, name, assignee, status);

    return task;
  }

  private void verifyTaskIndexIsUpdated(FlowableProcessInstance processInstance, JSONObject argumentObject, String name,
      String assignee, String status) throws WebClientException {
    assertFalse(argumentObject.isEmpty());
    assertTrue(StringUtils.isNotBlank(argumentObject.getString("id")));
    assertThat(argumentObject.getString("name"), is(name));
    assertThat(argumentObject.getString("assignee"), is(assignee));
    assertThat(argumentObject.getString("status"), is(status));

    var refProcessInstance = argumentObject.getJSONObject("processInstance");
    assertThat(refProcessInstance.getString("domain"), is("workflow"));
    assertThat(refProcessInstance.getString("id"), is(processInstance.getId()));
  }

  private void verifyProcessInstanceIndexIsUpdated(JSONObject argumentObject, String status) {
    assertFalse(argumentObject.isEmpty());
    assertTrue(StringUtils.isNotBlank(argumentObject.getString("id")));
    assertThat(argumentObject.getJSONObject("entity").getString("id"), is("entityid"));
    assertThat(argumentObject.getJSONObject("entity").getString("domain"), is("domain"));
    assertThat(argumentObject.getString("name"), is("name"));
    assertThat(argumentObject.getString("createdBy"), is("createdBy"));
    assertThat(argumentObject.getString("owner"), is("owner"));
    assertThat(argumentObject.getString("status"), is(status));
  }

  private void verifyDeployedDefinitionIsAddedToRegistry(JSONObject argumentObject) throws RegistryException {
    assertThat(argumentObject.length(), is(4));
    assertThat(argumentObject.getString("name"), is("domain.action"));
    assertThat(argumentObject.getString("resourceName"), is("domain.action.1.bpmn20.xml"));
    assertTrue(StringUtils.isNotBlank(argumentObject.getString("workflow")));
    assertTrue(StringUtils.isNotBlank(argumentObject.getString("content")));
  }
}
