package com.devit.mscore.workflow.flowable;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.devit.mscore.ApplicationContext;
import com.devit.mscore.Configuration;
import com.devit.mscore.DefaultApplicationContext;
import com.devit.mscore.Registry;
import com.devit.mscore.exception.ConfigException;
import com.devit.mscore.exception.ResourceException;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Optional;

import javax.sql.DataSource;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import io.zonky.test.db.postgres.junit.EmbeddedPostgresRules;
import io.zonky.test.db.postgres.junit.SingleInstancePostgresRule;

public class FlowableTest {

  private static final String VARIABLES = "{\"assignee\":\"assignee\",\"approver\":\"approver\",\"createdBy\":\"createdBy\",\"owner\":\"owner\",\"businessKey\":\"entityid\",\"name\":\"name\",\"domain\":\"domain\"}";

  @Rule
  public SingleInstancePostgresRule pgRule = EmbeddedPostgresRules.singleInstance();

  private FlowableWorkflowFactory factory;

  private ApplicationContext context;

  private DataSource dataSource;

  @Before
  public void setup() throws ConfigException {
    var configuration = mock(Configuration.class);
    doReturn(Optional.of("localhost")).when(configuration).getConfig("process.db.host");
    doReturn(Optional.of("5432")).when(configuration).getConfig("process.db.port");
    doReturn(Optional.of("flowable")).when(configuration).getConfig("process.db.name");
    doReturn(Optional.of("process")).when(configuration).getConfig("process.db.schema");
    doReturn(Optional.of("postgres")).when(configuration).getConfig("process.db.username");
    doReturn(Optional.of("postgres")).when(configuration).getConfig("process.db.password");
    doReturn(Optional.of("workflow")).when(configuration).getConfig("process.index.instance");
    doReturn(Optional.of("definition")).when(configuration).getConfig("process.definition.location");
    doReturn(true).when(configuration).has("workflow.definition.location");

    var registry = mock(Registry.class);
    this.factory = FlowableWorkflowFactory.of(configuration, registry);

    this.context = DefaultApplicationContext.of("test");
    this.dataSource = this.pgRule.getEmbeddedPostgres().getPostgresDatabase();
  }

  @Test
  public void integratedTest() throws ConfigException, URISyntaxException, ResourceException {
    try (MockedStatic<ApplicationContext> utilities = Mockito.mockStatic(ApplicationContext.class)) {
      utilities.when(() -> ApplicationContext.getContext()).thenReturn(this.context);

      // deploy definition
      var definitionRepository = this.factory.definitionRepository(this.dataSource);
      var definitionFile = getResource("definition/domain.action.1.bpmn20.xml");
      var definition = new FlowableDefinition(definitionFile);
      definitionRepository.deploy(definition);

      // create instance
      var instanceRepository = this.factory.instanceRepository(this.dataSource);
      var definitionIdOpt = definitionRepository.getDefinitionId("domain.action.1.bpmn20.xml");
      assertTrue(definitionIdOpt.isPresent());
      var instance = instanceRepository.create(definitionIdOpt.get(), new JSONObject(VARIABLES).toMap());
      assertNotNull(instance);

      // load instance
      var instanceOpt = instanceRepository.get(instance.getId());
      assertTrue(instanceOpt.isPresent());
      instance = instanceOpt.get();
      assertNotNull(instance);

      // check task
      var taskRepository = this.factory.taskRepository(this.dataSource);
      var tasks = taskRepository.getTasks(instance.getId());
      assertThat(tasks.size(), is(1));
      var task = (FlowableTask) tasks.get(0);
      assertThat(task.getName(), is("Test Approve"));
      assertThat(task.getAssignee(), is("approver"));

      // load task
      var taskOpt = taskRepository.getTask(task.getId());
      assertTrue(taskOpt.isPresent());
      var workflowTask = taskOpt.get();
      assertThat(workflowTask.getName(), is("Test Approve"));
      
      // complete task
      taskRepository.complete(task.getId(), Map.of("approved", true));
      tasks = taskRepository.getTasks(instance.getId());
      assertThat(tasks.size(), is(1));
      task = (FlowableTask) tasks.get(0);
      assertThat(task.getName(), is("Test Approved"));
      assertThat(task.getAssignee(), is("assignee"));

      taskRepository.complete(task.getId(), Map.of());

      // check instance completed
      instanceOpt = instanceRepository.get(instance.getId());
      assertFalse(instanceOpt.isPresent());
    }
  }

  private File getResource(String resourceName) throws URISyntaxException {
    var resource = getClass().getClassLoader().getResource(resourceName);
    return new File(resource.toURI());
  }
}
