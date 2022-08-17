package com.devit.mscore;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.devit.mscore.exception.ConfigException;
import com.devit.mscore.exception.RegistryException;
import com.devit.mscore.exception.ResourceException;

import java.io.File;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class ResourceManagerTest {

  private Registry registry;

  private Configuration configuration;

  private ResourceManager manager;

  @Before
  public void setup() {
    this.registry = mock(Registry.class);
    this.configuration = mock(Configuration.class);
    this.manager = new DummyResourceManager(this.configuration, this.registry);

    assertThat(this.manager.getType(), is("dummy"));
  }

  @Test
  public void testRegister() throws RegistryException, ResourceException, ConfigException {
    var location = getLocation("resource");
    doReturn(Optional.of(location)).when(this.configuration).getConfig("location");

    this.manager.registerResources();

    var captor = ArgumentCaptor.forClass(String.class);
    verify(this.registry, times(1)).add(anyString(), captor.capture());

    var argument = captor.getValue();
    var json = new JSONObject(argument);
    assertThat(json.getString("name"), is("resource.json"));
    assertTrue(StringUtils.isNoneBlank(json.getString("content")));
  }

  @Test
  public void testRegister_NoResourceConfig() throws ResourceException, RegistryException, ConfigException {
    doReturn(Optional.empty()).when(this.configuration).getConfig("location");

    var context = mock(ApplicationContext.class);
    doReturn("breadcrumbId").when(context).getBreadcrumbId();
    this.manager.registerResources();

    verifyNoInteractions(this.registry);
  }

  @Test
  public void testRegister_NoDirectory() throws ResourceException, RegistryException, ConfigException {
    doReturn(Optional.of("./nodir")).when(this.configuration).getConfig("location");

    var context = mock(ApplicationContext.class);
    doReturn("breadcrumbId").when(context).getBreadcrumbId();
    this.manager.registerResources();

    verifyNoInteractions(this.registry);
  }

  @Test
  public void testRegister_RegistryException() throws ResourceException, RegistryException, ConfigException {
    var location = getLocation("resource");
    doReturn(Optional.of(location)).when(this.configuration).getConfig("location");
    doThrow(new RegistryException("message")).when(this.registry).add(anyString(), anyString());

    var ex = assertThrows(ResourceException.class, () -> this.manager.registerResources());
    assertThat(ex.getMessage(), is("Cannot register resource."));
    assertThat(ex.getCause(), instanceOf(RegistryException.class));
  }

  private String getLocation(String location) {
    var classLoader = getClass().getClassLoader();
    var resource = classLoader.getResource(location);
    var file = new File(resource.getFile());
    return file.getAbsolutePath();
  }

  public static class DummyResourceManager extends ResourceManager {

    protected DummyResourceManager(Configuration configuration, Registry registry) {
      super("dummy", configuration, registry);
    }

    @Override
    protected String getResourceLocation() throws ConfigException {
      return this.configuration.getConfig("location").orElse(null);
    }

    @Override
    protected Resource createResource(File file) throws ResourceException {
      return new Resource(file);
    }
  }
}
