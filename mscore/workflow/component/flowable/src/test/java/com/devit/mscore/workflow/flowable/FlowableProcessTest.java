package com.devit.mscore.workflow.flowable;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.hamcrest.MatcherAssert.assertThat;
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

import java.io.File;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Optional;

import com.devit.mscore.ApplicationContext;
import com.devit.mscore.Configuration;
import com.devit.mscore.DefaultApplicationContext;
import com.devit.mscore.Registry;
import com.devit.mscore.ServiceRegistration;
import com.devit.mscore.exception.ConfigException;
import com.devit.mscore.exception.ProcessException;
import com.devit.mscore.exception.RegistryException;
import com.devit.mscore.exception.WebClientException;
import com.devit.mscore.web.Client;
import com.devit.mscore.workflow.flowable.delegate.TestDelegate;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import io.zonky.test.db.postgres.junit.EmbeddedPostgresRules;
import io.zonky.test.db.postgres.junit.SingleInstancePostgresRule;

public class FlowableProcessTest {

    private static final String TASK_INDEX = "workflowtask";

    private static final String INSTANCE_INDEX = "workflow";

    private static final String VARIABLES = "{\"assignee\":\"assignee\",\"approver\":\"approver\",\"createdBy\":\"createdBy\",\"owner\":\"owner\",\"businessKey\":\"entityid\",\"name\":\"name\",\"domain\":\"domain\"}";

    private static final String ENTITY = "{\"id\":\"entityid\",\"name\":\"name\"}";

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
    public void setup() throws RegistryException, ConfigException {
        this.configuration = mock(Configuration.class);
        doReturn(Optional.of("localhost")).when(this.configuration).getConfig(any(ApplicationContext.class), eq("process.db.host"));
        doReturn(Optional.of("5432")).when(this.configuration).getConfig(any(ApplicationContext.class), eq("process.db.port"));
        doReturn(Optional.of("flowable")).when(this.configuration).getConfig(any(ApplicationContext.class), eq("process.db.name"));
        doReturn(Optional.of("process")).when(this.configuration).getConfig(any(ApplicationContext.class), eq("process.db.schema"));
        doReturn(Optional.of("postgres")).when(this.configuration).getConfig(any(ApplicationContext.class), eq("process.db.username"));
        doReturn(Optional.of("postgres")).when(this.configuration).getConfig(any(ApplicationContext.class), eq("process.db.password"));
        doReturn(Optional.of(TASK_INDEX)).when(this.configuration).getConfig(any(ApplicationContext.class), eq("process.index.task"));
        doReturn(Optional.of(INSTANCE_INDEX)).when(this.configuration).getConfig(any(ApplicationContext.class), eq("process.index.instance"));
        doReturn(Optional.of("definition")).when(this.configuration).getConfig(any(ApplicationContext.class), eq("process.definition.location"));
        doReturn(true).when(this.configuration).has("workflow.definition.location");

        this.serviceRegistration = mock(ServiceRegistration.class);
        doReturn("http://data/domain").when(this.serviceRegistration).get(any(ApplicationContext.class), anyString());
        doReturn("http://data/workflow").when(this.serviceRegistration).get(any(ApplicationContext.class), eq("workflow"));
        doReturn("http://data/task").when(this.serviceRegistration).get(any(ApplicationContext.class), eq("task"));

        this.client = mock(Client.class);
        doReturn(this.client).when(this.client).createNew();

        this.dataClient = new DataClient(this.client, this.serviceRegistration, "workflow", "task");

        this.registry = mock(Registry.class);

        // This will be used to verify that TestHandler is executed.
        TestDelegate.registry = this.registry;

        this.process = getProcess();
        var map = new HashMap<String, Object>();
        map.put("principal", new JSONObject("{\"requestedBy\":\"createdBy\"}"));
        this.context = DefaultApplicationContext.of("test", map);

        // only for coverage
        this.dataClient.getDomainUri(this.context, "domain");
        this.process.getDomain();
        this.process.stop();
    }

    private FlowableProcess getProcess() {
        var dataSource = this.pgRule.getEmbeddedPostgres().getPostgresDatabase();
        var manager = FlowableWorkflowFactory.of(this.configuration, this.registry);
        return (FlowableProcess) manager.workflowProcess(dataSource, this.registry, this.dataClient);
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

        var ex = assertThrows(ProcessException.class, ()-> this.process.deployDefinition(this.context, flowableDefinition));
        assertThat(ex.getMessage(), is("Definition deployment failed."));
    }

    @Test
    public void testFlowableProcess_RegistryFailure() throws Exception {
        doThrow(new RegistryException("message")).when(this.registry).add(any(ApplicationContext.class), anyString(), anyString());

        // Start process engine
        this.process.start();

        // Deploy definition
        var definition = getResource("definition/domain.action.1.bpmn20.xml");
        var flowableDefinition = new FlowableDefinition(definition);

        var ex = assertThrows(ProcessException.class, ()-> this.process.deployDefinition(this.context, flowableDefinition));
        assertThat(ex.getMessage(), is("Cannot register process deployment."));
    }

    @Test
    public void testFlowableProcess_WebClientException_OnInstanceCreation() throws Exception {
        doThrow(new WebClientException("message")).when(this.client).post(any(ApplicationContext.class), anyString(), any());
        var argumentCaptor = ArgumentCaptor.forClass(String.class);

        // Start process engine
        this.process.start();

        // Deploy definition
        var definition = getResource("definition/domain.action.1.bpmn20.xml");
        var flowableDefinition = new FlowableDefinition(definition);
        this.process.deployDefinition(this.context, flowableDefinition);

        // Verify that deployed definition is added to registry
        verify(this.registry, times(1)).add(any(ApplicationContext.class), anyString(), argumentCaptor.capture());
        var argumentObject = new JSONObject(argumentCaptor.getValue());
        assertThat(argumentObject.length(), is(4));
        assertThat(argumentObject.getString("name"), is("domain.action"));
        assertThat(argumentObject.getString("resourceName"), is("domain.action.1.bpmn20.xml"));
        assertTrue(StringUtils.isNotBlank(argumentObject.getString("workflow")));
        assertTrue(StringUtils.isNotBlank(argumentObject.getString("content")));

        // Create instance
        this.process.createInstance(this.context, argumentObject.getString("workflow"), new JSONObject(VARIABLES).toMap());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testFlowableProcess_WebClientException_OnTaskCompletion() throws Exception {
        var response = new JSONObject("{\"code\":200,\"payload\":\"success\"}");
        when(this.client.post(any(ApplicationContext.class), anyString(), any())).thenReturn(response).thenThrow(new WebClientException("message"));
        var resourceArgumentCaptor = ArgumentCaptor.forClass(String.class);
        var taskArgumentCaptor = ArgumentCaptor.forClass(Optional.class);
        var instanceArgumentCaptor = ArgumentCaptor.forClass(Optional.class);

        // Start process engine
        this.process.start();

        // Deploy definition
        var definition = getResource("definition/domain.action.1.bpmn20.xml");
        var flowableDefinition = new FlowableDefinition(definition);
        this.process.deployDefinition(this.context, flowableDefinition);

        // Verify that deployed definition is added to registry
        verify(this.registry, times(1)).add(any(ApplicationContext.class), anyString(), resourceArgumentCaptor.capture());
        var argumentObject = new JSONObject(resourceArgumentCaptor.getValue());
        assertThat(argumentObject.length(), is(4));
        assertThat(argumentObject.getString("name"), is("domain.action"));
        assertThat(argumentObject.getString("resourceName"), is("domain.action.1.bpmn20.xml"));
        assertTrue(StringUtils.isNotBlank(argumentObject.getString("workflow")));
        assertTrue(StringUtils.isNotBlank(argumentObject.getString("content")));

        // Create instance
        var processInstance = this.process.createInstance(this.context, argumentObject.getString("workflow"),
                new JSONObject(VARIABLES).toMap());
        assertNotNull(processInstance);

        // Verify instance is indexed
        verify(this.client, times(1)).post(any(ApplicationContext.class), eq("http://data/workflow"), instanceArgumentCaptor.capture());
        argumentObject = (JSONObject) instanceArgumentCaptor.getValue().get();
        assertFalse(argumentObject.isEmpty());
        assertTrue(StringUtils.isNotBlank(argumentObject.getString("id")));
        assertThat(argumentObject.getJSONObject("entity").getString("id"), is("entityid"));
        assertThat(argumentObject.getString("name"), is("name"));
        assertThat(argumentObject.getString("createdBy"), is("createdBy"));
        assertThat(argumentObject.getString("status"), is("Active"));

        // verify task is indexed
        verify(this.client, times(1)).post(any(ApplicationContext.class), eq("http://data/task"), taskArgumentCaptor.capture());
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
        var tasks = this.process.getTasks(this.context, processInstance.getId());
        assertThat(tasks.size(), is(1));
        var task = tasks.get(0);
        assertThat(task.getName(), is("Test Approve"));

        // Complete task and get next task
        var taskResponse = "{\"approved\":true}";
        this.process.completeTask(this.context, task.getId(), new JSONObject(taskResponse));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testFlowableProcess() throws Exception {
        var response = new JSONObject("{\"code\":200,\"payload\":\"success\"}");
        when(this.client.post(any(ApplicationContext.class), anyString(), any()))
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
        this.process.deployDefinition(this.context, flowableDefinition);

        // Verify that deployed definition is added to registry
        verify(this.registry, times(1)).add(any(ApplicationContext.class), anyString(), registryArgumentCaptor.capture());
        var argumentObject = new JSONObject(registryArgumentCaptor.getValue());
        assertThat(argumentObject.length(), is(4));
        assertThat(argumentObject.getString("name"), is("domain.action"));
        assertThat(argumentObject.getString("resourceName"), is("domain.action.1.bpmn20.xml"));
        assertTrue(StringUtils.isNotBlank(argumentObject.getString("workflow")));
        assertTrue(StringUtils.isNotBlank(argumentObject.getString("content")));

        doReturn(argumentObject.toString()).when(this.registry).get(any(ApplicationContext.class), eq("domain.action"));

        // Create instance
        var processInstance = this.process.createInstanceByAction(this.context, argumentObject.getString("name"),
                new JSONObject(ENTITY), new JSONObject(VARIABLES).toMap());
        assertNotNull(processInstance);

        // Verify task is created
        var tasks = this.process.getTasks(this.context, processInstance.getId());
        assertThat(tasks.size(), is(1));
        var task = tasks.get(0);
        assertThat(task.getName(), is("Test Approve"));

        // Verify instance is indexed
        verify(this.client, times(1)).post(any(ApplicationContext.class), eq("http://data/workflow"), instanceArgumentCaptor.capture());
        argumentObject = (JSONObject) instanceArgumentCaptor.getValue().get();
        assertFalse(argumentObject.isEmpty());
        assertTrue(StringUtils.isNotBlank(argumentObject.getString("id")));
        assertThat(argumentObject.getJSONObject("entity").getString("id"), is("entityid"));
        assertThat(argumentObject.getString("name"), is("name"));
        assertThat(argumentObject.getString("createdBy"), is("createdBy"));
        assertThat(argumentObject.getString("status"), is("Active"));

        // Verify task is indexed
        verify(this.client, times(1)).post(any(ApplicationContext.class), eq("http://data/task"), taskArgumentCaptor.capture());
        argumentObject = (JSONObject) taskArgumentCaptor.getValue().get();
        assertFalse(argumentObject.isEmpty());
        assertTrue(StringUtils.isNotBlank(argumentObject.getString("id")));
        assertThat(argumentObject.getString("name"), is("Test Approve"));
        assertThat(argumentObject.getString("assignee"), is("approver"));
        assertThat(argumentObject.getString("status"), is("Active"));

        var refProcessInstance = argumentObject.getJSONObject("processInstance");
        assertThat(refProcessInstance.getString("domain"), is("workflow"));
        assertThat(refProcessInstance.getString("id"), is(processInstance.getId()));

        // Complete task and get next task
        var taskResponse = "{\"approved\":true}";
        this.process.completeTask(this.context, task.getId(), new JSONObject(taskResponse));

        // Verify next task is created
        tasks = this.process.getTasks(this.context, processInstance.getId());
        assertThat(tasks.size(), is(1));
        task = tasks.get(0);
        assertThat(task.getName(), is("Test Approved"));

        // Verify indexing
        verify(this.client, times(3)).post(any(ApplicationContext.class), eq("http://data/task"), taskArgumentCaptor.capture());
        var arguments = taskArgumentCaptor.getAllValues();
        assertThat(arguments.size(), is(4));

        // Verify current task index is updated
        argumentObject = (JSONObject) arguments.get(2).get();
        assertFalse(argumentObject.isEmpty());
        assertTrue(StringUtils.isNotBlank(argumentObject.getString("id")));
        assertThat(argumentObject.getString("name"), is("Test Approve"));
        assertThat(argumentObject.getString("assignee"), is("approver"));
        assertThat(argumentObject.getString("status"), is("Complete"));

        refProcessInstance = argumentObject.getJSONObject("processInstance");
        assertThat(refProcessInstance.getString("domain"), is("workflow"));
        assertThat(refProcessInstance.getString("id"), is(processInstance.getId()));

        // Verify next task is indexed
        argumentObject = (JSONObject) arguments.get(3).get();
        assertFalse(argumentObject.isEmpty());
        assertTrue(StringUtils.isNotBlank(argumentObject.getString("id")));
        assertThat(argumentObject.getString("name"), is("Test Approved"));
        assertThat(argumentObject.getString("assignee"), is("assignee"));
        assertThat(argumentObject.getString("status"), is("Active"));

        refProcessInstance = argumentObject.getJSONObject("processInstance");
        assertThat(refProcessInstance.getString("domain"), is("workflow"));
        assertThat(refProcessInstance.getString("id"), is(processInstance.getId()));

        // Verify that Java Delegate is called
        // verify(this.repository, times(1)).hashCode();

        // Complete final task
        this.process.completeTask(this.context, task.getId(), new JSONObject());

        // Verify current index is updated
        verify(this.client, times(4)).post(any(ApplicationContext.class), eq("http://data/task"), taskArgumentCaptor.capture());
        argumentObject = (JSONObject) taskArgumentCaptor.getValue().get();
        assertFalse(argumentObject.isEmpty());
        assertTrue(StringUtils.isNotBlank(argumentObject.getString("id")));
        assertThat(argumentObject.getString("name"), is("Test Approved"));
        assertThat(argumentObject.getString("assignee"), is("assignee"));
        assertThat(argumentObject.getString("status"), is("Complete"));

        refProcessInstance = argumentObject.getJSONObject("processInstance");
        assertThat(refProcessInstance.getString("domain"), is("workflow"));
        assertThat(refProcessInstance.getString("id"), is(processInstance.getId()));

        // Verify all task is completed
        tasks = this.process.getTasks(this.context, processInstance.getId());
        assertThat(tasks.size(), is(0));

        // Verify the process instance is completed
        var processInstanceId = processInstance.getId();
        processInstance = this.process.getInstance(this.context, processInstanceId);
        assertNull(processInstance);

        // Verify process instance is moved to history
        var historicProcessInstance = this.process.getHistoricInstance(processInstanceId);
        assertNotNull(historicProcessInstance);

        // Verify instance index is updated
        // verify(this.instanceIndex, times(2)).index(any(ApplicationContext.class), instanceArgumentCaptor.capture());
        // argument = instanceArgumentCaptor.getValue();
        // assertFalse(argument.isEmpty());
        // assertTrue(StringUtils.isNotBlank(argument.getString("id")));
        // assertThat(argument.getJSONObject("entity").getString("id"), is("entityid"));
        // // assertThat(argument.getJSONObject("entity").getString("domain"), is("domain"));
        // assertThat(argument.getString("name"), is("name"));
        // assertThat(argument.getString("createdBy"), is("createdBy"));
        // assertThat(argument.getString("owner"), is("owner"));
        // assertThat(argument.getString("status"), is("Complete"));
    }
}
