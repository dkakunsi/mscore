package com.devit.mscore.registry;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.io.IOException;

import com.devit.mscore.ApplicationContext;
import com.devit.mscore.DefaultApplicationContext;
import com.devit.mscore.exception.RegistryException;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.apache.curator.test.TestingServer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class ZookeeperRegistryTest {

    private static TestingServer zookeeperServer;

    private static ZookeeperRegistry registry;

    private static ApplicationContext context;

    @BeforeClass
    public static void startServer() throws Exception {
        zookeeperServer = new TestingServer(4000);
        zookeeperServer.start();

        var sleepMsBetweenRetries = 100;
        var maxRetries = 3;
        var retryPolicy = new RetryNTimes(maxRetries, sleepMsBetweenRetries);
        
        var client = CuratorFrameworkFactory.newClient("127.0.0.1:4000", retryPolicy);
        
        context = DefaultApplicationContext.of("test");
        registry = new ZookeeperRegistry(context, "curator", client);
        registry.open();
    }

    @AfterClass
    public static void stopServer() throws IOException {
        registry.close();
        zookeeperServer.stop();
    }

    @Test
    public void test_Found() throws RegistryException {
        var key = "/service/domain/address";
        registry.add(context, key, "domain-address");
        var value = registry.get(context, key);

        assertThat(value, is("domain-address"));
    }

    @Test
    public void test_NotFound() throws RegistryException {
        var value = registry.get(context, "/notexists");
        assertNull(value);
    }

    @Test
    public void testAdd_ThrowException() throws RegistryException {
        var mockedClient = mock(CuratorFramework.class);
        doThrow(IllegalArgumentException.class).when(mockedClient).create();
        
        var localRegistry = new ZookeeperRegistry("name", mockedClient);
        assertThrows(RegistryException.class, () -> localRegistry.add(context, "key", "value"));
    }

    @Test
    public void dummyTest() throws RegistryException {
        var name = registry.getName();
        registry.all(context);
        assertThat(name, is("curator"));

        var values = registry.values(context);
        assertNotNull(values);

        var keys = registry.keys(context);
        assertNotNull(keys);
    }

    @Test
    public void testGetCacheKey() {
        var key = "/services/data/config.name";
        var expectedCacheKey = "services.data.config.name";

        var cacheKey = ZookeeperRegistry.getCacheKey(key);
        assertThat(cacheKey, is(expectedCacheKey));
    }
}
