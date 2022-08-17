package com.devit.mscore.schema.everit;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Optional;

import com.devit.mscore.Configuration;
import com.devit.mscore.Registry;
import com.devit.mscore.exception.ConfigException;
import com.devit.mscore.exception.RegistryException;
import com.devit.mscore.exception.ResourceException;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class SchemaManagerTest {

  private Registry registry;

  private Configuration configuration;

  private SchemaManager manager;

  @Before
  public void setup() {
    this.registry = mock(Registry.class);
    this.configuration = mock(Configuration.class);
    doReturn("data").when(this.configuration).getServiceName();
    doReturn(true).when(this.configuration).has("services.data.schema.resource.location");
    this.manager = SchemaManager.of(this.configuration, this.registry);
  }

  @Test
  public void testRegisterSchema() throws ConfigException, URISyntaxException, ResourceException, RegistryException {
    var location = getLocation("registration");
    doReturn(Optional.of(location)).when(this.configuration).getConfig("services.data.schema.resource.location");

    this.manager.registerResources();

    assertThat(this.manager.getSchemas().size(), is(1));
    assertThat(this.manager.getSchemas().get(0).getDomain(), is("resource"));

    var captor = ArgumentCaptor.forClass(String.class);
    verify(this.registry, times(1)).add(anyString(), captor.capture());

    var argument = new JSONObject(captor.getValue());
    assertThat(argument.getString("name"), is("resource"));
    assertFalse(StringUtils.isBlank(argument.getString("content")));
  }

  @Test
  public void testGetSchema() throws RegistryException, URISyntaxException, ResourceException {
    var resourceFile = getResourceFile("resource/resource.json");
    var content = new JSONSchema(resourceFile).getContent();

    var mockedSchema = new JSONObject().put("id", "id").put("name", "name").put("content", content);
    doReturn(mockedSchema.toString()).when(this.registry).get("domain");

    var schema = this.manager.getSchema("domain");
    assertThat(schema.getDomain(), is("name"));
    assertThat(schema.getName(), is("name"));
    assertTrue(StringUtils.isNotBlank(schema.getContent()));
  }

  private String getLocation(String location) throws URISyntaxException {
    return getResourceFile(location).getAbsolutePath();
  }

  private static File getResourceFile(String resourceName) throws URISyntaxException {
    var resource = SchemaManagerTest.class.getClassLoader().getResource(resourceName);
    return new File(resource.toURI());
  }

}
