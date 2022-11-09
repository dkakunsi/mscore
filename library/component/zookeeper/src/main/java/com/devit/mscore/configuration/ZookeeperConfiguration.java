package com.devit.mscore.configuration;

import com.devit.mscore.Configuration;
import com.devit.mscore.Registry;
import com.devit.mscore.exception.ConfigException;
import com.devit.mscore.exception.RegistryException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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
      configs = registry.all();
    } catch (RegistryException ex) {
      configs = new HashMap<>();
      throw new ConfigException(ex);
    }
  }

  @Override
  public String getPrefixSeparator() {
    return ".";
  }

  @Override
  public Map<String, String> getConfigs() {
    return new HashMap<>(configs);
  }

  @Override
  public String getServiceName() {
    return serviceName;
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

    var registryKey = getRegistryKey(key);
    return executeGetConfig(registryKey);
  }

  private String getRegistryKey(String key) {
    var keyElements = key.split("\\.");
    var oldGroupElement = String.join(".", keyElements[0], keyElements[1]) + ".";
    var newGroupELement = "/" + String.join("/", keyElements[0], keyElements[1]) + "/";
    return key.replace(oldGroupElement, newGroupELement);
  }

  private Optional<String> executeGetConfig(String registryKey) throws ConfigException {
    try {
      var registryValue = registry.get(registryKey);
      return registryValue != null ? Optional.of(registryValue) : Optional.empty();
    } catch (RegistryException ex) {
      throw new ConfigException(ex);
    }
  }
}
