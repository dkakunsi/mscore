package com.devit.mscore.workflow.flowable;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.sql.DataSource;

import com.devit.mscore.ApplicationContext;
import com.devit.mscore.Configuration;
import com.devit.mscore.Registry;
import com.devit.mscore.Resource;
import com.devit.mscore.ResourceManager;
import com.devit.mscore.WorkflowDefinition;
import com.devit.mscore.WorkflowProcess;
import com.devit.mscore.exception.ConfigException;
import com.devit.mscore.exception.RegistryException;
import com.devit.mscore.exception.ResourceException;

import org.postgresql.ds.PGSimpleDataSource;

public class FlowableWorkflowFactory extends ResourceManager {

    private static final String DB_HOST = "workflow.db.host";

    private static final String DB_PORT = "workflow.db.port";

    private static final String DB_NAME = "workflow.db.name";

    private static final String DB_SCHEMA = "workflow.db.schema";

    private static final String DB_USERNAME = "workflow.db.username";

    private static final String DB_PASSWORD = "workflow.db.password";

    private static final String LOCATION = "workflow.definition.location";

    protected FlowableWorkflowFactory(Configuration configuration, Registry registry) {
        super("workflow_definition", configuration, registry);
    }

    public static FlowableWorkflowFactory of(Configuration configuration, Registry registry) {
        return new FlowableWorkflowFactory(configuration, registry);
    }

    public WorkflowProcess workflowProcess(ApplicationContext context, Registry registry, DataClient dataClient) throws ConfigException {
        var dataSource = getDataSource(context);
        return workflowProcess(dataSource, registry, dataClient);
    }

    public WorkflowProcess workflowProcess(DataSource dataSource, Registry registry, DataClient dataClient) {
        return new FlowableProcess(dataSource, registry, dataClient);
    }

    private DataSource getDataSource(ApplicationContext context) throws ConfigException {
        var dataSource = new PGSimpleDataSource();
        dataSource.setServerNames(getDataSourceConfig(context, DB_HOST).split(","));
        dataSource.setPortNumbers(getPorts(context));
        dataSource.setUser(getDataSourceConfig(context, DB_USERNAME));
        dataSource.setPassword(getDataSourceConfig(context, DB_PASSWORD));
        dataSource.setCurrentSchema(getDataSourceConfig(context, DB_SCHEMA));
        dataSource.setDatabaseName(getDataSourceConfig(context, DB_NAME));
        return dataSource;
    }

    private int[] getPorts(ApplicationContext context) throws ConfigException {
        var ports = getDataSourceConfig(context, DB_PORT).split(",");
        return Arrays.stream(ports).mapToInt(Integer::parseInt).toArray();
    }

    private String getDataSourceConfig(ApplicationContext context, String configName) throws ConfigException {
        return this.configuration.getConfig(context, configName).orElse("");
    }

    public List<WorkflowDefinition> getDefinitions(ApplicationContext context) throws RegistryException {
        var registeredDefinitions = this.registry.all(context);
        var definitions = new ArrayList<WorkflowDefinition>();
        registeredDefinitions.forEach((name, definition) -> definitions.add(new FlowableDefinition(definition)));
        return definitions;
    }

    @Override
    protected String getResourceLocation(ApplicationContext context) {
        try {
            return this.configuration.getConfig(context, LOCATION).orElse(null);
        } catch (ConfigException ex) {
            return null;
        }
    }

    @Override
    protected Resource createResource(File file) throws ResourceException {
        return new FlowableDefinition(file);
    }
}
