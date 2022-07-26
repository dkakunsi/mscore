package com.devit.mscore.registry;

import com.devit.mscore.Logger;
import com.devit.mscore.Registry;
import com.devit.mscore.exception.ApplicationRuntimeException;
import com.devit.mscore.exception.RegistryException;
import com.devit.mscore.logging.ApplicationLogger;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;

public class ZookeeperRegistry implements Registry {

  private static final Logger LOG = ApplicationLogger.getLogger(ZookeeperRegistry.class);

  private static final String ROOT_PATH = "/";

  private String name;

  private CuratorFramework client;

  private Map<String, String> cache;

  public ZookeeperRegistry(String name, String zkHost, RetryPolicy retryPolicy) throws RegistryException {
    this.name = name;
    client = CuratorFrameworkFactory.newClient(zkHost, retryPolicy);
    cache = new HashMap<>();
    init();
  }

  public void init() throws RegistryException {
    open();
    load(ROOT_PATH);
  }

  private void load(String path) throws RegistryException {
    // get will put the value into cache
    get(path);
    loadChildren(path);
  }

  private void loadChildren(String parentPath) throws RegistryException {
    try {
      var children = client.getChildren().forPath(parentPath);
      for (var child : children) {
        var format = ROOT_PATH.equals(parentPath) ? "%s%s" : "%s/%s";
        var key = String.format(format, parentPath, child);
        load(key);
      }
    } catch (Exception ex) {
      throw new RegistryException(ex);
    }
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void add(String key, String value) throws RegistryException {
    try {
      var stat = client.checkExists().forPath(key);
      if (stat == null) {
        client.create().creatingParentsIfNeeded().forPath(key, value.getBytes(StandardCharsets.UTF_8.name()));
      } else {
        client.setData().forPath(key, value.getBytes(StandardCharsets.UTF_8.name()));
      }

      clearCache(getCacheKey(key));
    } catch (Exception ex) {
      throw new RegistryException("Cannot create register", ex);
    }
  }

  private void clearCache(String cacheKey) {
    cache.remove(cacheKey);
  }

  static String getCacheKey(String key) {
    if (ROOT_PATH.equals(key)) {
      return "root";
    }

    var cacheKey = key.substring(1); // remove leading /
    return cacheKey.replace("/", ".");
  }

  @Override
  public String get(String registryKey) throws RegistryException {
    var cacheKey = getCacheKey(registryKey);

    cache.computeIfAbsent(cacheKey, key -> {
      try {
        var stat = client.checkExists().forPath(registryKey);
        if (stat == null) {
          LOG.warn("No configuration value for key '{}'", registryKey);
          return null;
        }
        var value = client.getData().forPath(registryKey);
        return value != null ? new String(value, StandardCharsets.UTF_8.name()) : null;
      } catch (Exception ex) {
        throw new ApplicationRuntimeException(new RegistryException("Cannot get register", ex));
      }
    });

    return cache.get(cacheKey);
  }

  @Override
  public Map<String, String> all() {
    return new HashMap<>(cache);
  }

  @Override
  public void open() {
    if (!isClientOpened()) {
      client.start();
    }
  }

  private boolean isClientOpened() {
    return client.getState().equals(CuratorFrameworkState.STARTED);
  }

  @Override
  public void close() {
    client.close();
  }

  @Override
  public List<String> values() throws RegistryException {
    return new ArrayList<>(cache.values());
  }

  @Override
  public List<String> keys() throws RegistryException {
    return new ArrayList<>(cache.keySet());
  }
}
