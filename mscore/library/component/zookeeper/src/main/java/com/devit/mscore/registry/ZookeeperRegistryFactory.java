package com.devit.mscore.registry;

import com.devit.mscore.Configuration;
import com.devit.mscore.exception.ConfigException;
import com.devit.mscore.exception.RegistryException;

import java.util.Optional;

import org.apache.curator.retry.RetryNTimes;

public class ZookeeperRegistryFactory {

  private static final String ZOOKEEPER_HOST = "zookeeper.host";

  private static final String SLEEP_BETWEEN_RETRY = "zookeeper.retry.sleep";

  private static final String MAX_RETRY = "zookeeper.retry.max";

  private static final Integer DEFAULT_SLEEP_BETWEEN_RESTART = 100;

  private static final Integer DEFAULT_MAX_RETRY = 3;

  private Configuration configuration;

  private ZookeeperRegistryFactory(Configuration configuration) {
    this.configuration = configuration;
  }

  public static ZookeeperRegistryFactory of(Configuration configuration) {
    return new ZookeeperRegistryFactory(configuration);
  }

  public ZookeeperRegistry registry(String registryName) throws RegistryException {
    var sleepMsBetweenRetries = getSleepBetweenRetry(configuration);
    var maxRetries = getMaxRetry(configuration);
    var retryPolicy = new RetryNTimes(maxRetries, sleepMsBetweenRetries);
    return new ZookeeperRegistry(registryName, getZookeeperHost(configuration), retryPolicy);
  }

  static String getZookeeperHost(Configuration configuration)
      throws RegistryException {
    try {
      var zookeperHost = configuration.getConfig(ZOOKEEPER_HOST);
      return zookeperHost.orElseThrow(() -> new RegistryException("No zookeeper host was configured."));
    } catch (ConfigException ex) {
      throw new RegistryException(ex);
    }
  }

  static Integer getSleepBetweenRetry(Configuration configuration)
      throws RegistryException {
    try {
      var configuredSleepBetweenRetry = configuration.getConfig(SLEEP_BETWEEN_RETRY);
      return getOrDefault(configuredSleepBetweenRetry, DEFAULT_SLEEP_BETWEEN_RESTART);
    } catch (ConfigException ex) {
      throw new RegistryException(ex);
    }
  }

  static Integer getMaxRetry(Configuration configuration) throws RegistryException {
    try {
      var configuredMaxRetry = configuration.getConfig(MAX_RETRY);
      return getOrDefault(configuredMaxRetry, DEFAULT_MAX_RETRY);
    } catch (ConfigException ex) {
      throw new RegistryException(ex);
    }
  }

  static Integer getOrDefault(Optional<String> optionalValue, Integer defaultValue) {
    try {
      return optionalValue.isPresent() ? Integer.valueOf(optionalValue.get()) : defaultValue;
    } catch (NumberFormatException ex) {
      return defaultValue;
    }
  }
}
