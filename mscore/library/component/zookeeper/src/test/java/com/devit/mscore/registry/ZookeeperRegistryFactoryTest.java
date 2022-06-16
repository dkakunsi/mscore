package com.devit.mscore.registry;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.util.Optional;

import com.devit.mscore.ApplicationContext;
import com.devit.mscore.Configuration;
import com.devit.mscore.DefaultApplicationContext;
import com.devit.mscore.exception.ConfigException;
import com.devit.mscore.exception.RegistryException;

import org.apache.curator.test.TestingServer;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ZookeeperRegistryFactoryTest {
    
    private static TestingServer zookeeperServer;

    private Configuration configuration;

    private ZookeeperRegistryFactory factory;

    private ApplicationContext context;

    @BeforeClass
    public static void startServer() throws Exception {
        zookeeperServer = new TestingServer(4000);
        zookeeperServer.start();
    }

    @AfterClass
    public static void stopServer() throws IOException {
        zookeeperServer.stop();
    }

    @Before
    public void setup() {
        this.configuration = mock(Configuration.class);
        this.factory = ZookeeperRegistryFactory.of(this.configuration);
        this.context = DefaultApplicationContext.of("test");
    }

    @Test
    public void testGetZookeeperRegistry() throws RegistryException, ConfigException {
        doReturn(Optional.of("127.0.0.1:4000")).when(this.configuration).getConfig(any(ApplicationContext.class), eq("zookeeper.host"));
        doReturn(Optional.of("50")).when(this.configuration).getConfig(any(ApplicationContext.class), eq("zookeeper.retry.sleep"));
        doReturn(Optional.of("1")).when(this.configuration).getConfig(any(ApplicationContext.class), eq("zookeeper.retry.max"));

        var domainRegistry = this.factory.registry(this.context, "domain");

        assertNotNull(domainRegistry);
    }

    @Test
    public void testGetZookeeperRegistry_UseDefaultValues() throws RegistryException, ConfigException {
        doReturn(Optional.of("127.0.0.1:4000")).when(this.configuration).getConfig(any(ApplicationContext.class), eq("zookeeper.host"));
        var domainRegistry = this.factory.registry(this.context, "domain");
        assertNotNull(domainRegistry);
    }

    @Test
    public void testGetZookeeperRegistry_NoZookeeperHost_ThrowRegistryException() {
        var ex = assertThrows(RegistryException.class, () -> this.factory.registry(this.context, "domain"));
        var exceptionMessage = ex.getMessage();
        assertThat(exceptionMessage, is("No zookeeper host was configured."));
    }

    @Test
    public void testGetZookeeperHost_ThrowException() throws ConfigException {
        var context = DefaultApplicationContext.of("test");
        var configuration = mock(Configuration.class);
        doThrow(new ConfigException("Error message.")).when(configuration).getConfig(context, "zookeeper.host");

        var ex = assertThrows(RegistryException.class, () -> ZookeeperRegistryFactory.getZookeeperHost(context, configuration));
        assertThat(ex.getMessage(), is("Error message."));
        assertThat(ex.getCause(), instanceOf(ConfigException.class));
    }

    @Test
    public void testGetSleepBetweenReply_ThrowException() throws ConfigException {
        var context = DefaultApplicationContext.of("test");
        var configuration = mock(Configuration.class);
        doThrow(new ConfigException("Error message.")).when(configuration).getConfig(context, "zookeeper.retry.sleep");

        var ex = assertThrows(RegistryException.class, () -> ZookeeperRegistryFactory.getSleepBetweenRetry(context, configuration));
        assertThat(ex.getMessage(), is("Error message."));
        assertThat(ex.getCause(), instanceOf(ConfigException.class));
    }

    @Test
    public void testGetMaxRetry_ThrowException() throws ConfigException {
        var context = DefaultApplicationContext.of("test");
        var configuration = mock(Configuration.class);
        doThrow(new ConfigException("Error message.")).when(configuration).getConfig(context, "zookeeper.retry.max");

        var ex = assertThrows(RegistryException.class, () -> ZookeeperRegistryFactory.getMaxRetry(context, configuration));
        assertThat(ex.getMessage(), is("Error message."));
        assertThat(ex.getCause(), instanceOf(ConfigException.class));
    }

    @Test
    public void testGetOrDefault_ThrowException() {
        var result = ZookeeperRegistryFactory.getOrDefault(Optional.of("###"), 1);
        assertThat(result, is(Integer.valueOf(1)));
    }
}