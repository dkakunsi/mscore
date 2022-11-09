package com.devit.mscore.workflow.flowable;

import static org.flowable.common.engine.impl.AbstractEngineConfiguration.DB_SCHEMA_UPDATE_TRUE;

import com.devit.mscore.Configuration;
import com.devit.mscore.Registry;
import com.devit.mscore.Resource;
import com.devit.mscore.ResourceManager;
import com.devit.mscore.WorkflowDataSource;
import com.devit.mscore.WorkflowDefinition;
import com.devit.mscore.WorkflowDefinitionRepository;
import com.devit.mscore.WorkflowInstanceRepository;
import com.devit.mscore.WorkflowTaskRepository;
import com.devit.mscore.exception.ConfigException;
import com.devit.mscore.exception.ProcessException;
import com.devit.mscore.exception.RegistryException;
import com.devit.mscore.exception.ResourceException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.flowable.engine.ProcessEngine;
import org.flowable.engine.ProcessEngineConfiguration;
import org.flowable.engine.impl.cfg.StandaloneProcessEngineConfiguration;

public class FlowableWorkflowFactory extends ResourceManager {

  private static final String LOCATION = "services.%s.definition.location";

  private static final String WORKFLOW_DEFINITION = "workflow_definition";

  private ProcessEngine processEngine;

  private WorkflowDataSource<?> dataSource;

  protected FlowableWorkflowFactory(Configuration configuration, Registry registry, WorkflowDataSource<?> dataSource) {
    super(WORKFLOW_DEFINITION, configuration, registry);
    this.dataSource = dataSource;
  }

  public static FlowableWorkflowFactory of(Configuration configuration, Registry registry,
      WorkflowDataSource<?> dataSource) {
    return new FlowableWorkflowFactory(configuration, registry, dataSource);
  }

  public List<WorkflowDefinition> getDefinitions() throws RegistryException {
    var registeredDefinitions = registry.values();
    var definitions = new ArrayList<WorkflowDefinition>();
    registeredDefinitions.forEach(definition -> definitions.add(new FlowableDefinition(definition)));
    return definitions;
  }

  @Override
  protected String getResourceLocation() {
    var configName = String.format(LOCATION, configuration.getServiceName());
    try {
      return configuration.getConfig(configName).orElse(null);
    } catch (ConfigException ex) {
      return null;
    }
  }

  @Override
  protected Resource createResource(File file) throws ResourceException {
    return new FlowableDefinition(file);
  }

  private ProcessEngine getProcessEngine(WorkflowDataSource<?> dataSource) throws ConfigException, ProcessException {
    if (processEngine != null) {
      return processEngine;
    }

    ProcessEngineConfiguration processEngineConfiguration;
    if (dataSource.getType().equals(WorkflowDataSource.Type.SQL)) {
      processEngineConfiguration = new StandaloneProcessEngineConfiguration()
          .setDataSource((DataSource) dataSource.get())
          .setDatabaseSchemaUpdate(DB_SCHEMA_UPDATE_TRUE);
    } else {
      throw new ProcessException("Data source is not supported");
    }

    return processEngine = processEngineConfiguration.buildProcessEngine();
  }

  public WorkflowDefinitionRepository definitionRepository() throws ConfigException, ProcessException {
    return new FlowableDefinitionRepository(getProcessEngine(dataSource));
  }

  public WorkflowInstanceRepository instanceRepository() throws ConfigException, ProcessException {
    return new FlowableInstanceRepository(getProcessEngine(dataSource));
  }

  public WorkflowTaskRepository taskRepository() throws ConfigException, ProcessException {
    return new FlowableTaskRepository(getProcessEngine(dataSource));
  }
}
