package com.devit.mscore.workflow.flowable;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.sql.DataSource;

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

    private static final String DB_HOST = "platform.postgres.host";

    private static final String DB_PORT = "platform.postgres.port";

    private static final String DB_USERNAME = "platform.postgres.username";

    private static final String DB_PASSWORD = "platform.postgres.password";

    private static final String DB_NAME = "services.%s.db.name";

    private static final String DB_SCHEMA = "services.%s.db.schema";

    private static final String LOCATION = "services.%s.definition.location";

    private static final String DEFAULT_PORT = "5432";

    protected FlowableWorkflowFactory(Configuration configuration, Registry registry) {
        super("workflow_definition", configuration, registry);
    }

    public static FlowableWorkflowFactory of(Configuration configuration, Registry registry) {
        return new FlowableWorkflowFactory(configuration, registry);
    }

    public WorkflowProcess workflowProcess(DataClient dataClient) throws ConfigException {
        var dataSource = getDataSource();
        return workflowProcess(dataSource, dataClient);
    }

    public WorkflowProcess workflowProcess(DataSource dataSource, DataClient dataClient) {
        return new FlowableProcess(dataSource, this.registry, dataClient);
    }

    private DataSource getDataSource() throws ConfigException {
        var serviceName = this.configuration.getServiceName();

        var dataSource = new PGSimpleDataSource();
        dataSource.setServerNames(this.configuration.getConfig(DB_HOST).orElseThrow(() -> new ConfigException("No psql host provided")).split(","));
        dataSource.setPortNumbers(getPorts());
        dataSource.setUser(this.configuration.getConfig(DB_USERNAME).orElseThrow(() -> new ConfigException("No psql user provided")));
        dataSource.setPassword(this.configuration.getConfig(DB_PASSWORD).orElseThrow(() -> new ConfigException("No psql password provided")));
        dataSource.setCurrentSchema(this.configuration.getConfig(String.format(DB_SCHEMA, serviceName)).orElseThrow(() -> new ConfigException("No psql schema configured")));
        dataSource.setDatabaseName(this.configuration.getConfig(String.format(DB_NAME, serviceName)).orElseThrow(() -> new ConfigException("No psql database configured")));
        return dataSource;
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
}
