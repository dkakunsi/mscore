package com.devit.mscore.registry;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import com.devit.mscore.exception.RegistryException;

import java.io.IOException;
import java.util.List;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.ExistsBuilder;
import org.apache.curator.framework.api.GetChildrenBuilder;
import org.apache.curator.framework.api.GetDataBuilder;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.retry.RetryNTimes;
import org.apache.curator.test.TestingServer;
import org.apache.zookeeper.data.Stat;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

public class ZookeeperRegistryTest {

  private static TestingServer zookeeperServer;

  private static ZookeeperRegistry registry;

  @BeforeClass
  public static void startServer() throws Exception {
    zookeeperServer = new TestingServer(4000);
    zookeeperServer.start();

    var sleepMsBetweenRetries = 100;
    var maxRetries = 3;
    var retryPolicy = new RetryNTimes(maxRetries, sleepMsBetweenRetries);

    registry = new ZookeeperRegistry("curator", "127.0.0.1:4000", retryPolicy);
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
    registry.add(key, "domain-address");
    var value = registry.get(key);

    assertThat(value, is("domain-address"));
  }

  @Test
  public void test_NotFound() throws RegistryException {
    var value = registry.get("/notexists");
    assertNull(value);
  }

  @Test
  public void testAdd_ThrowException() throws Exception {
    var mockedExistsBuilder = mock(ExistsBuilder.class);
    doReturn(new Stat()).when(mockedExistsBuilder).forPath(anyString());
    var mockedGetDataBuilder = mock(GetDataBuilder.class);
    doReturn(null).when(mockedGetDataBuilder).forPath(anyString());
    var mockedGetChildrenBuilder = mock(GetChildrenBuilder.class);
    doReturn(List.of()).when(mockedGetChildrenBuilder).forPath(anyString());

    var mockedClient = mock(CuratorFramework.class);
    doReturn(mockedExistsBuilder).when(mockedClient).checkExists();
    doReturn(mockedGetDataBuilder).when(mockedClient).getData();
    doReturn(mockedGetChildrenBuilder).when(mockedClient).getChildren();
    doReturn(CuratorFrameworkState.STARTED).when(mockedClient).getState();
    doThrow(IllegalArgumentException.class).when(mockedClient).create();

    try (MockedStatic<CuratorFrameworkFactory> utilities = Mockito.mockStatic(CuratorFrameworkFactory.class)) {
      utilities.when(() -> CuratorFrameworkFactory.newClient(anyString(), any(RetryPolicy.class)))
          .thenReturn(mockedClient);

      var localRegistry = new ZookeeperRegistry("name", "host", mock(RetryPolicy.class));
      assertThrows(RegistryException.class, () -> localRegistry.add("key", "value"));
    }
  }

  @Test
  public void dummyTest() throws RegistryException {
    var name = registry.getName();
    registry.all();
    assertThat(name, is("curator"));

    var values = registry.values();
    assertNotNull(values);

    var keys = registry.keys();
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
