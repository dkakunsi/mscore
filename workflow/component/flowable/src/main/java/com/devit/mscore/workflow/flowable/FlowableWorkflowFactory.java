package com.devit.mscore.workflow.flowable;

import static org.flowable.common.engine.impl.AbstractEngineConfiguration.DB_SCHEMA_UPDATE_TRUE;

import com.devit.mscore.Configuration;
import com.devit.mscore.Registry;
import com.devit.mscore.Resource;
import com.devit.mscore.ResourceManager;
import com.devit.mscore.WorkflowDefinition;
import com.devit.mscore.WorkflowDefinitionRepository;
import com.devit.mscore.WorkflowInstanceRepository;
import com.devit.mscore.WorkflowTaskRepository;
import com.devit.mscore.exception.ConfigException;
import com.devit.mscore.exception.RegistryException;
import com.devit.mscore.exception.ResourceException;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.sql.DataSource;

import org.flowable.engine.ProcessEngine;
import org.flowable.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.postgresql.ds.PGSimpleDataSource;

public class FlowableWorkflowFactory extends ResourceManager {

  private static final String DB_HOST = "platform.postgres.host";

  private static final String DB_PORT = "platform.postgres.port";

  private static final String DB_USERNAME = "platform.postgres.username";

  private static final String DB_PASSWORD = "platform.postgres.password";

  private static final String DB_NAME = "services.%s.db.name";

  private static final String DB_SCHEMA = "services.%s.db.schema";

  private static final String LOCATION = "services.%s.definition.location";

  private static final String DEFAULT_PORT = "5432";

  private ProcessEngine processEngine;

  private DataSource dataSource;

  protected FlowableWorkflowFactory(Configuration configuration, Registry registry) {
    super("workflow_definition", configuration, registry);
  }

  public static FlowableWorkflowFactory of(Configuration configuration, Registry registry) {
    return new FlowableWorkflowFactory(configuration, registry);
  }

  DataSource getDataSource() throws ConfigException {
    if (this.dataSource == null) {
      var serviceName = this.configuration.getServiceName();
      var serverNames = this.configuration.getConfig(DB_HOST)
          .orElseThrow(() -> new ConfigException("No psql host provided")).split(",");
      var portNumbers = getPorts();
      var user = this.configuration.getConfig(DB_USERNAME)
          .orElseThrow(() -> new ConfigException("No psql user provided"));
      var password = this.configuration.getConfig(DB_PASSWORD)
          .orElseThrow(() -> new ConfigException("No psql password provided"));
      var schemaName = this.configuration.getConfig(String.format(DB_SCHEMA, serviceName))
          .orElseThrow(() -> new ConfigException("No psql schema configured"));
      var databaseName = this.configuration.getConfig(String.format(DB_NAME, serviceName))
          .orElseThrow(() -> new ConfigException("No psql database configured"));

      this.dataSource = getDataSource(serverNames, portNumbers, user, password, schemaName, databaseName);
    }
    return this.dataSource;
  }

  private DataSource getDataSource(String[] serverNames, int[] portNumbers, String user, String password,
      String schemaName, String databaseName) {
    var pgDataSource = new PGSimpleDataSource();
    pgDataSource.setServerNames(serverNames);
    pgDataSource.setPortNumbers(portNumbers);
    pgDataSource.setUser(user);
    pgDataSource.setPassword(password);
    pgDataSource.setCurrentSchema(schemaName);
    pgDataSource.setDatabaseName(databaseName);
    return pgDataSource;
  }

  private int[] getPorts() throws ConfigException {
    var ports = this.configuration.getConfig(DB_PORT).orElse(DEFAULT_PORT).split(",");
    return Arrays.stream(ports).mapToInt(Integer::parseInt).toArray();
  }

  public List<WorkflowDefinition> getDefinitions() throws RegistryException {
    var registeredDefinitions = this.registry.values();
    var definitions = new ArrayList<WorkflowDefinition>();
    registeredDefinitions.forEach(definition -> definitions.add(new FlowableDefinition(definition)));
    return definitions;
  }

  @Override
  protected String getResourceLocation() {
    var configName = String.format(LOCATION, this.configuration.getServiceName());
    try {
      return this.configuration.getConfig(configName).orElse(null);
    } catch (ConfigException ex) {
      return null;
    }
  }

  @Override
  protected Resource createResource(File file) throws ResourceException {
    return new FlowableDefinition(file);
  }

  private ProcessEngine getProcessEngine(DataSource dataSource) {
    if (this.processEngine == null) {
      // @formatter:off
      var processEngineConfiguration = new StandaloneProcessEngineConfiguration()
          .setDataSource(dataSource)
          .setDatabaseSchemaUpdate(DB_SCHEMA_UPDATE_TRUE);
      // @formatter:on

      this.processEngine = processEngineConfiguration.buildProcessEngine();
    }
    return this.processEngine;
  }

  public WorkflowDefinitionRepository definitionRepository() throws ConfigException {
    return definitionRepository(getDataSource());
  }

  public WorkflowDefinitionRepository definitionRepository(DataSource dataSource) {
    return new FlowableDefinitionRepository(getProcessEngine(dataSource));
  }

  public WorkflowInstanceRepository instanceRepository() throws ConfigException {
    return instanceRepository(getDataSource());
  }

  public WorkflowInstanceRepository instanceRepository(DataSource dataSource) {
    return new FlowableInstanceRepository(getProcessEngine(dataSource));
  }

  public WorkflowTaskRepository taskRepository() throws ConfigException {
    return taskRepository(getDataSource());
  }

  public WorkflowTaskRepository taskRepository(DataSource dataSource) {
    return new FlowableTaskRepository(getProcessEngine(dataSource));
  }
}
