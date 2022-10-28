package com.devit.mscore.workflow;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.devit.mscore.Publisher;
import com.devit.mscore.Registry;
import com.devit.mscore.WorkflowDefinition;
import com.devit.mscore.WorkflowDefinitionRepository;
import com.devit.mscore.WorkflowInstance;
import com.devit.mscore.WorkflowInstanceRepository;
import com.devit.mscore.WorkflowTask;
import com.devit.mscore.WorkflowTaskRepository;
import com.devit.mscore.exception.ApplicationException;
import com.devit.mscore.exception.ProcessException;
import com.devit.mscore.workflow.service.WorkflowServiceImpl;

import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

public class WorkflowServiceTest {

  private WorkflowServiceImpl service;

  private WorkflowDefinitionRepository definitionRepository;

  private WorkflowInstanceRepository instanceRepository;

  private WorkflowTaskRepository taskRepository;

  private Registry registry;

  private Publisher publisher;

  @Before
  public void setup() throws ApplicationException, CloneNotSupportedException {
    this.publisher = mock(Publisher.class);
    this.registry = mock(Registry.class);
    doReturn(this.registry).when(this.registry).clone();

    this.definitionRepository = mock(WorkflowDefinitionRepository.class);
    this.instanceRepository = mock(WorkflowInstanceRepository.class);
    this.taskRepository = mock(WorkflowTaskRepository.class);
    this.service = new WorkflowServiceImpl(this.registry, this.publisher, this.definitionRepository,
        this.instanceRepository, this.taskRepository);
  }

  @Test
  public void testFailedDeployment_ThrowException() throws Exception {
    var definition = mock(WorkflowDefinition.class);
    doReturn(false).when(this.definitionRepository).isExists(any(WorkflowDefinition.class));
    doThrow(new RuntimeException()).when(this.definitionRepository).deploy(any(WorkflowDefinition.class));

    var ex = assertThrows(ProcessException.class, () -> this.service.deployDefinition(definition));
    assertThat(ex.getMessage(), is("Definition deployment failed"));
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
    assertThat(ex.getMessage(), is("Cannot register process deployment"));
  }
}
