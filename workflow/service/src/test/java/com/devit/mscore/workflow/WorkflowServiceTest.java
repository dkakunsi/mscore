package com.devit.mscore.workflow;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.devit.mscore.ApplicationContext;
import com.devit.mscore.DataClient;
import com.devit.mscore.Registry;
import com.devit.mscore.WorkflowDefinition;
import com.devit.mscore.WorkflowDefinitionRepository;
import com.devit.mscore.WorkflowInstance;
import com.devit.mscore.WorkflowInstanceRepository;
import com.devit.mscore.WorkflowTask;
import com.devit.mscore.WorkflowTaskRepository;
import com.devit.mscore.exception.ApplicationException;
import com.devit.mscore.exception.ProcessException;
import com.devit.mscore.exception.WebClientException;
import com.devit.mscore.web.Client;
import com.devit.mscore.workflow.service.WorkflowServiceImpl;

import java.util.List;
import java.util.Optional;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

public class WorkflowServiceTest {

  private static final String VARIABLES = "{\"assignee\":\"assignee\",\"approver\":\"approver\",\"createdBy\":\"createdBy\",\"owner\":\"owner\",\"businessKey\":\"entityid\",\"name\":\"name\",\"domain\":\"domain\"}";

  private static final String ENTITY = "{\"id\":\"entityid\",\"name\":\"name\",\"domain\":\"domain\"}";

  private ApplicationContext context;

  private WorkflowServiceImpl service;

  private WorkflowDefinitionRepository definitionRepository;

  private WorkflowInstanceRepository instanceRepository;

  private WorkflowTaskRepository taskRepository;

  private Registry registry;

  private DataClient dataClient;

  private Client client;

  @Before
  public void setup() throws ApplicationException, CloneNotSupportedException {
    this.context = mock(ApplicationContext.class);

    this.client = mock(Client.class);
    this.dataClient = mock(DataClient.class);
    doReturn(this.client).when(this.dataClient).getClient();
    doReturn("http://data/workflow").when(this.dataClient).getWorkflowUri();

    this.registry = mock(Registry.class);
    doReturn(this.registry).when(this.registry).clone();

    this.definitionRepository = mock(WorkflowDefinitionRepository.class);
    this.instanceRepository = mock(WorkflowInstanceRepository.class);
    this.taskRepository = mock(WorkflowTaskRepository.class);
    this.service = new WorkflowServiceImpl(this.registry, this.dataClient, this.definitionRepository,
        this.instanceRepository, this.taskRepository);
  }

  @Test
  public void testFailedDeployment_ThrowException() throws Exception {
    var definition = mock(WorkflowDefinition.class);
    doReturn(false).when(this.definitionRepository).isExists(any(WorkflowDefinition.class));
    doThrow(new RuntimeException()).when(this.definitionRepository).deploy(any(WorkflowDefinition.class));

    var ex = assertThrows(ProcessException.class, () -> this.service.deployDefinition(definition));
    assertThat(ex.getMessage(), is("Definition deployment failed."));
  }

  @Test
  public void testDeployingExistingDefinition_ShouldSuccess() throws Exception {
    var definition = mock(WorkflowDefinition.class);
    doReturn("definitionName").when(definition).getName();
    doReturn("resourceName").when(definition).getResourceName();
    doReturn(true).when(this.definitionRepository).isExists(any(WorkflowDefinition.class));
    doReturn(Optional.of("definitionId")).when(this.definitionRepository).getDefinitionId(anyString());

    this.service.deployDefinition(definition);
    verify(this.registry).add("definitionName", "definitionId");
  }

  @Test
  public void testGet_InstanceAndTasks() {
    var mockedInstance = mock(WorkflowInstance.class);
    doReturn(Optional.of(mockedInstance)).when(this.instanceRepository).get(anyString());
    var instance = this.service.getInstance("instanecId");
    assertTrue(instance.isPresent());

    var mockedTask = mock(WorkflowTask.class);
    doReturn(List.of(mockedTask)).when(this.taskRepository).getTasks(anyString());
    var tasks = this.service.getTasks("instanceId");
    assertFalse(tasks.isEmpty());
  }

  @Test
  public void testFailedRegistration_DefinitionIdNotFound() throws Exception {
    var definition = mock(WorkflowDefinition.class);
    doReturn(Optional.empty()).when(this.definitionRepository).getDefinitionId(anyString());

    var ex = assertThrows(ProcessException.class, () -> this.service.deployDefinition(definition));
    assertThat(ex.getMessage(), is("Cannot register process deployment."));
  }

  @Test
  public void testFailedSynchronization() throws Exception {
    try (MockedStatic<ApplicationContext> utilities = Mockito.mockStatic(ApplicationContext.class)) {
      utilities.when(() -> ApplicationContext.getContext()).thenReturn(this.context);

      doThrow(new WebClientException("message")).when(this.client).post(anyString(), any());
      var createdInstance = mock(WorkflowInstance.class);
      doReturn(new JSONObject()).when(createdInstance).toJson(anyList());
      doReturn(createdInstance).when(this.instanceRepository).create(anyString(), anyMap());
      var instance = this.service.createInstance("definitionId", new JSONObject(ENTITY),
          new JSONObject(VARIABLES).toMap());
      assertNotNull(instance);
    }
  }
}
