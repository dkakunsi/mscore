package com.devit.mscore.configuration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Map;

import com.devit.mscore.ApplicationContext;
import com.devit.mscore.DefaultApplicationContext;
import com.devit.mscore.exception.ConfigException;
import com.devit.mscore.exception.RegistryException;
import com.devit.mscore.registry.ZookeeperRegistry;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class ZookeeperConfigurationTest {

    private ZookeeperRegistry registry;

    private ApplicationContext context;

    @Before
    public void setup() {
        this.registry = mock(ZookeeperRegistry.class);
        this.context = DefaultApplicationContext.of("test");
    }

    @Test
    public void testGetConfig() throws RegistryException, ConfigException {
        doReturn("value").when(this.registry).get(any(ApplicationContext.class), anyString());

        var configuration = new ZookeeperConfiguration(this.registry, "test");
        var configValue = configuration.getConfig(this.context, "type", "service", "key");
        assertThat(configValue.get(), is("value"));

        var pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(this.registry).get(eq(this.context), pathCaptor.capture());

        assertThat(pathCaptor.getValue(), is("/type/service/key"));
    }

    @Test
    public void testGetConfig_Fail() throws RegistryException, ConfigException {
        doThrow(RegistryException.class).when(this.registry).get(any(ApplicationContext.class), anyString());

        var configuration = new ZookeeperConfiguration(this.registry, "test");
        var ex = assertThrows(ConfigException.class, () -> configuration.getConfig(this.context, "type", "service", "key"));
        var actualEx = ex.getCause();
        assertThat(actualEx, instanceOf(RegistryException.class));
    }

    @Test
    public void testGetAll() throws ConfigException {
        doReturn(Map.of("key", "value")).when(this.registry).all(any(ApplicationContext.class));

        var configuration = new ZookeeperConfiguration(this.context, this.registry, "test");
        var configs = configuration.getConfigs();

        assertThat(configs.size(), is(1));
        assertThat(configs.get("key"), is("value"));
    }

    @Test
    public void testInit_Fail() throws RegistryException {
        doThrow(RegistryException.class).when(this.registry).init(any(ApplicationContext.class));

        var ex = assertThrows(ConfigException.class, () -> new ZookeeperConfiguration(this.context, this.registry, "test"));
        var actualEx = ex.getCause();
        assertThat(actualEx, instanceOf(RegistryException.class));
    }

    @Test
    public void testGetConfig_NullValue_ShouldReturnEmpty() throws ConfigException, RegistryException {
        doReturn(null).when(this.registry).get(any(ApplicationContext.class), anyString());
        var configuration = new ZookeeperConfiguration(this.context, this.registry, "test");
        var optionalValue = configuration.getConfig(this.context, "key");
        assertThat(optionalValue.isEmpty(), is(true));
    }
}
