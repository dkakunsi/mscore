package com.devit.mscore.workflow.flowable;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.io.File;
import java.util.HashMap;
import java.util.Optional;

import com.devit.mscore.ApplicationContext;
import com.devit.mscore.Configuration;
import com.devit.mscore.DefaultApplicationContext;
import com.devit.mscore.Registry;
import com.devit.mscore.ServiceRegistration;
import com.devit.mscore.exception.ConfigException;
import com.devit.mscore.exception.RegistryException;
import com.devit.mscore.exception.ResourceException;
import com.devit.mscore.web.Client;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

public class FlowableWorkflowFactoryTest {

    private Configuration configuration;

    private Registry registry;

    private DataClient dataClient;

    private ServiceRegistration serviceRegistration;

    private Client client;

    private FlowableWorkflowFactory workflowFactory;

    private ApplicationContext context;

    @Before
    public void setup() throws RegistryException, ConfigException {
        this.context = DefaultApplicationContext.of("test");
        this.configuration = mock(Configuration.class);
        doReturn(Optional.of("localhost")).when(this.configuration).getConfig(this.context, "workflow.db.host");
        doReturn(Optional.of("5432")).when(this.configuration).getConfig(this.context, "workflow.db.port");
        doReturn(Optional.of("flowable")).when(this.configuration).getConfig(this.context, "workflow.db.name");
        doReturn(Optional.of("process")).when(this.configuration).getConfig(this.context, "workflow.db.schema");
        doReturn(Optional.of("postgres")).when(this.configuration).getConfig(this.context, "workflow.db.username");
        doReturn(Optional.of("postgres")).when(this.configuration).getConfig(this.context, "workflow.db.password");
        doReturn(Optional.of("taskIndex")).when(this.configuration).getConfig(this.context, "workflow.index.task");
        doReturn(true).when(this.configuration).has("workflow.definition.location");

        this.serviceRegistration = mock(ServiceRegistration.class);
        doReturn("toBeReturned").when(this.serviceRegistration).get(any(ApplicationContext.class), anyString());

        this.client = mock(Client.class);
        this.dataClient = new DataClient(this.client, this.serviceRegistration, "workflow", "task");
        this.registry = mock(Registry.class);

        this.workflowFactory = FlowableWorkflowFactory.of(this.configuration, this.registry);
        var workflowProcess = workflowFactory.workflowProcess(this.context, this.registry, this.dataClient);
        assertNotNull(workflowProcess);
    }

    @Test
    public void testRegisterDefinition() throws ResourceException, RegistryException, ConfigException {
        var context = DefaultApplicationContext.of("test");
        var location = getLocation("definition");
        doReturn(Optional.of(location)).when(this.configuration).getConfig(context, "workflow.definition.location");

        this.workflowFactory.registerResources(context);
        verify(this.registry, times(1)).add(any(ApplicationContext.class), anyString(), anyString());
    }

    @Test
    public void testRegisterDefinition_NoDefinitionConfig() throws ResourceException, RegistryException, ConfigException {
        doReturn(Optional.of("")).when(this.configuration).getConfig(this.context, "workflow.definition.location");

        var context = mock(ApplicationContext.class);
        doReturn("breadcrumbId").when(context).getBreadcrumbId();
        this.workflowFactory.registerResources(context);

        verifyNoInteractions(this.registry);
    }

    @Test
    public void testRegisterDefinition_NoDirectory() throws ResourceException, RegistryException, ConfigException {
        doReturn(Optional.of("./nodir")).when(this.configuration).getConfig(this.context, "workflow.definition.location");

        var context = mock(ApplicationContext.class);
        doReturn("breadcrumbId").when(context).getBreadcrumbId();
        this.workflowFactory.registerResources(context);

        verifyNoInteractions(this.registry);
    }

    @Test
    public void testGetDefinitions_FromRegistry() throws ConfigException, ResourceException, RegistryException {
        var context = DefaultApplicationContext.of("test");

        var object = new JSONObject("{\"id\":\"id\",\"name\":\"name\",\"resourceName\":\"resourceName\",\"content\":\"content\"}");
        var map = new HashMap<>();
        map.put(object.getString("name"), object.toString());
        doReturn(map).when(this.registry).all(context);

        var definitions = this.workflowFactory.getDefinitions(context);

        assertThat(definitions.size(), is(1));
        var definition = definitions.get(0);
        assertThat(definition.getName(), is("name"));
        assertThat(definition.getResourceName(), is("resourceName"));
        assertThat(definition.getContent(), is("content"));
    }

    private String getLocation(String location) {
        var classLoader = getClass().getClassLoader();
        var resource = classLoader.getResource(location);
        var file = new File(resource.getFile());
        return file.getAbsolutePath();
    }
}
