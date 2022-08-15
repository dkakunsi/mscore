package com.devit.mscore.template.pebble;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Optional;

import com.devit.mscore.Configuration;
import com.devit.mscore.Registry;
import com.devit.mscore.exception.ConfigException;
import com.devit.mscore.exception.ResourceException;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

public class PebbleTemplateFactoryTest {

    private Registry registry;

    private Configuration configuration;

    private PebbleTemplateFactory factory;

    @Before
    public void setup() {
        this.registry = mock(Registry.class);
        this.configuration = mock(Configuration.class);
        doReturn("notification").when(this.configuration).getServiceName();
        doReturn(true).when(this.configuration).has("services.notification.template.resource.location");
        this.factory = PebbleTemplateFactory.of(this.registry, this.configuration);
    }

    @Test
    public void testCreateTemplate() {
        var template = this.factory.template();
        assertNotNull(template);
    }

    @Test
    public void testGetResourceLocation() throws ConfigException {
        doReturn(Optional.of("./template")).when(this.configuration).getConfig("services.notification.template.resource.location");

        var location = this.factory.getResourceLocation();
        assertThat(location, is("./template"));
    }

    @Test
    public void testCreateResource() throws URISyntaxException, ResourceException {
        var resourceFile = getResourceFile("template/domain.action.txt");

        var content = new PebbleTemplateResource(resourceFile).getContent();
        var json = new JSONObject("{\"name\":\"domain.action\"}").put("content", content);

        var resource = this.factory.createResource(resourceFile);

        var actual = resource.getMessage();
        assertThat(actual.toString(), is(json.toString()));
    }

    public static File getResourceFile(String resourceName) throws URISyntaxException {
        var resource = PebbleTemplateFactoryTest.class.getClassLoader().getResource(resourceName);
        return new File(resource.toURI());
    }
}
