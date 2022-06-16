package com.devit.mscore.registry;

import java.util.Optional;

import com.devit.mscore.ApplicationContext;
import com.devit.mscore.Configuration;
import com.devit.mscore.exception.ConfigException;
import com.devit.mscore.exception.RegistryException;

import org.apache.curator.framework.CuratorFrameworkFactory;
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

    public ZookeeperRegistry registry(ApplicationContext context, String registryName) throws RegistryException {
        var sleepMsBetweenRetries = getSleepBetweenRetry(context, configuration);
        var maxRetries = getMaxRetry(context, configuration);
        var retryPolicy = new RetryNTimes(maxRetries, sleepMsBetweenRetries);

        var client = CuratorFrameworkFactory.newClient(getZookeeperHost(context, configuration), retryPolicy);
        return new ZookeeperRegistry(registryName, client);
    }

    static String getZookeeperHost(ApplicationContext context, Configuration configuration)
            throws RegistryException {
        try {
            var zookeperHost = configuration.getConfig(context, ZOOKEEPER_HOST);
            return zookeperHost.orElseThrow(() -> new RegistryException("No zookeeper host was configured."));
        } catch (ConfigException ex) {
            throw new RegistryException(ex);
        }
    }

    static Integer getSleepBetweenRetry(ApplicationContext context, Configuration configuration)
            throws RegistryException {
        try {
            var configuredSleepBetweenRetry = configuration.getConfig(context, SLEEP_BETWEEN_RETRY);
            return getOrDefault(configuredSleepBetweenRetry, DEFAULT_SLEEP_BETWEEN_RESTART);
        } catch (ConfigException ex) {
            throw new RegistryException(ex);
        }
    }

    static Integer getMaxRetry(ApplicationContext context, Configuration configuration) throws RegistryException {
        try {
            var configuredMaxRetry = configuration.getConfig(context, MAX_RETRY);
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
