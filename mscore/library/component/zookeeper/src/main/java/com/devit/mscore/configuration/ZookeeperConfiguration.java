package com.devit.mscore.configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.devit.mscore.ApplicationContext;
import com.devit.mscore.Configuration;
import com.devit.mscore.exception.ConfigException;
import com.devit.mscore.exception.RegistryException;
import com.devit.mscore.registry.ZookeeperRegistry;

import org.apache.commons.lang3.StringUtils;

public class ZookeeperConfiguration implements Configuration {

    private ZookeeperRegistry registry;

    private Map<String, String> configs;

    private String serviceName;

    public ZookeeperConfiguration(ZookeeperRegistry registry, String serviceName) {
        this.registry = registry;
        this.serviceName = serviceName;
    }

    public ZookeeperConfiguration(ApplicationContext context, ZookeeperRegistry registry, String serviceName) throws ConfigException {
        this(registry, serviceName);
        init(context);
    }

    private void init(ApplicationContext context) throws ConfigException {
        try {
            this.registry.init(context);
            this.configs = this.registry.all(context);
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
    public Optional<String> getConfig(ApplicationContext context, String type, String service, String key) throws ConfigException {
        var path = String.format("/%s/%s/%s", type, service, key);
        return executeGetConfig(context, path);
    }

    @Override
    public Optional<String> getConfig(ApplicationContext context, String key) throws ConfigException {
        if (!StringUtils.contains(key, ".")) {
            return Optional.empty();
        }

        var registryKey = "/" + key.replace(".", "/");
        return executeGetConfig(context, registryKey);
    }

    private Optional<String> executeGetConfig(ApplicationContext context, String registryKey) throws ConfigException {
        try {
            var registryValue = this.registry.get(context, registryKey);
            return registryValue != null ? Optional.of(registryValue) : Optional.empty();
        } catch (RegistryException ex) {
            throw new ConfigException(ex);
        }
    }
}
