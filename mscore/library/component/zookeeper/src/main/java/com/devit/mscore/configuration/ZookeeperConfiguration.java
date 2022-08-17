package com.devit.mscore.configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.devit.mscore.Configuration;
import com.devit.mscore.Registry;
import com.devit.mscore.exception.ConfigException;
import com.devit.mscore.exception.RegistryException;

import org.apache.commons.lang3.StringUtils;

public class ZookeeperConfiguration implements Configuration {

  private Registry registry;

  private Map<String, String> configs;

  private String serviceName;

  public ZookeeperConfiguration(Registry registry, String serviceName) throws ConfigException {
    this.registry = registry;
    this.serviceName = serviceName;
    init();
  }

  private void init() throws ConfigException {
    try {
      this.configs = this.registry.all();
    } catch (RegistryException ex) {
      this.configs = new HashMap<>();
      throw new ConfigException(ex);
    }
  }

  @Override
  public String getPrefixSeparator() {
    return ".";
  }

  @Override
  public Map<String, String> getConfigs() {
    return this.configs;
  }

  @Override
  public String getServiceName() {
    return this.serviceName;
  }

  @Override
  public Optional<String> getConfig(String type, String service, String key) throws ConfigException {
    var path = String.format("/%s/%s/%s", type, service, key);
    return executeGetConfig(path);
  }

  @Override
  public Optional<String> getConfig(String key) throws ConfigException {
    if (!StringUtils.contains(key, ".")) {
      return Optional.empty();
    }

    var registryKey = "/" + key.replace(".", "/");
    return executeGetConfig(registryKey);
  }

  private Optional<String> executeGetConfig(String registryKey) throws ConfigException {
    try {
      var registryValue = this.registry.get(registryKey);
      return registryValue != null ? Optional.of(registryValue) : Optional.empty();
    } catch (RegistryException ex) {
      throw new ConfigException(ex);
    }
  }
}
