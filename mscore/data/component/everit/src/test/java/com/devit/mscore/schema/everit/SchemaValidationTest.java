package com.devit.mscore.schema.everit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.net.URISyntaxException;

import com.devit.mscore.ApplicationContext;
import com.devit.mscore.DefaultApplicationContext;
import com.devit.mscore.Registry;
import com.devit.mscore.exception.ApplicationRuntimeException;
import com.devit.mscore.exception.RegistryException;
import com.devit.mscore.exception.ResourceException;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

public class SchemaValidationTest {

    private Registry registry;

    private ApplicationContext context;

    @Before
    public void setup() {
        this.registry = mock(Registry.class);
        this.context = DefaultApplicationContext.of("test");
    }

    @Test
    public void testValidate() throws URISyntaxException, ResourceException, RegistryException {
        // @formatter:off
        var resourceFile = getResourceFile("resource/resource.json");
        var schema = new JSONObject()
            .put("id", "id")
            .put("name", "name")
            .put("content", new JSONSchema(resourceFile).getContent());
        doReturn(schema.toString()).when(this.registry).get(any(ApplicationContext.class), anyString());
        // @formatter:on

        var validation = new SchemaValidation(this.registry);
        var input = "{\"domain\":\"domain\",\"id\":\"id\",\"name\":\"name\",\"reference1\":{\"domain\":\"referenceDomain1\",\"id\":\"referenceId1\"}}";

        var result = validation.validate(this.context, new JSONObject(input));
        assertTrue(result);
    }

    @Test
    public void testValidate_Invalid() throws URISyntaxException, ResourceException, RegistryException {
        // @formatter:off
        var resourceFile = getResourceFile("resource/resource.json");
        var schema = new JSONObject()
            .put("id", "id")
            .put("name", "name")
            .put("content", new JSONSchema(resourceFile).getContent());
        doReturn(schema.toString()).when(this.registry).get(any(ApplicationContext.class), anyString());
        // @formatter:on

        var validation = new SchemaValidation(this.registry);
        var input = "{\"domain\":\"unknown\",\"id\":\"toolongid\",\"name\":\"toolongname\"}";

        var result = validation.validate(this.context, new JSONObject(input));
        assertFalse(result);
    }

    @Test
    public void testValidate_NoSchema() throws URISyntaxException, ResourceException, RegistryException {
        doThrow(RegistryException.class).when(this.registry).get(any(ApplicationContext.class), anyString());

        var validation = new SchemaValidation(this.registry);
        var input = "{\"domain\":\"unknown\",\"id\":\"toolongid\",\"name\":\"toolongname\"}";

        var json = new JSONObject(input);
        var ex = assertThrows(ApplicationRuntimeException.class, () -> validation.validate(this.context, json));
        assertThat(ex.getCause(), instanceOf(RegistryException.class));
    }

    public static File getResourceFile(String resourceName) throws URISyntaxException {
        var resource = SchemaValidationTest.class.getClassLoader().getResource(resourceName);
        return new File(resource.toURI());
    }

}
