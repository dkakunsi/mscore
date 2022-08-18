package com.devit.mscore.configuration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.devit.mscore.Registry;
import com.devit.mscore.exception.ConfigException;
import com.devit.mscore.exception.RegistryException;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class ZookeeperConfigurationTest {

  private Registry registry;

  @Before
  public void setup() {
    this.registry = mock(Registry.class);
  }

  @Test
  public void testGetConfig() throws RegistryException, ConfigException {
    doReturn("value").when(this.registry).get(anyString());

    var configuration = new ZookeeperConfiguration(this.registry, "test");
    var configValue = configuration.getConfig("type", "service", "key");
    assertThat(configValue.get(), is("value"));

    var pathCaptor = ArgumentCaptor.forClass(String.class);
    verify(this.registry).get(pathCaptor.capture());

    assertThat(pathCaptor.getValue(), is("/type/service/key"));
  }

  @Test
  public void testGetConfig_Fail() throws RegistryException, ConfigException {
    doThrow(RegistryException.class).when(this.registry).get(anyString());

    var configuration = new ZookeeperConfiguration(this.registry, "test");
    var ex = assertThrows(ConfigException.class, () -> configuration.getConfig("type", "service", "key"));
    var actualEx = ex.getCause();
    assertThat(actualEx, instanceOf(RegistryException.class));
  }

  @Test
  public void testGetAll() throws ConfigException, RegistryException {
    doReturn(Map.of("key", "value")).when(this.registry).all();

    var configuration = new ZookeeperConfiguration(this.registry, "test");
    var configs = configuration.getConfigs();

    assertThat(configs.size(), is(1));
    assertThat(configs.get("key"), is("value"));
  }

  @Test
  public void testInit_Fail() throws RegistryException {
    doThrow(RegistryException.class).when(this.registry).all();

    var ex = assertThrows(ConfigException.class, () -> new ZookeeperConfiguration(this.registry, "test"));
    var actualEx = ex.getCause();
    assertThat(actualEx, instanceOf(RegistryException.class));
  }

  @Test
  public void testGetConfig_NullValue_ShouldReturnEmpty() throws ConfigException, RegistryException {
    doReturn(null).when(this.registry).get(anyString());
    var configuration = new ZookeeperConfiguration(this.registry, "test");
    var optionalValue = configuration.getConfig("key");
    assertThat(optionalValue.isEmpty(), is(true));
  }

  @Test
  public void testGetConfigByKey() throws ConfigException, RegistryException {
    var mockedRegistry = mock(Registry.class);
    doReturn("serviceGroup1").when(mockedRegistry).get("/platform/kafka/group.id");

    var configuration = new ZookeeperConfiguration(mockedRegistry, "serviceName");
    assertThat(configuration.getPrefixSeparator(), is("."));
    assertThat(configuration.getServiceName(), is("serviceName"));

    var configValue = configuration.getConfig("platform.kafka.group.id");
    assertThat(configValue.isPresent(), is(true));
    assertThat(configValue.get(), is("serviceGroup1"));
  }
}
