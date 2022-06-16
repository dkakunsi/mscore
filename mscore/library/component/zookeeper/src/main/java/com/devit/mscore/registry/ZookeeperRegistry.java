package com.devit.mscore.registry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.devit.mscore.ApplicationContext;
import com.devit.mscore.Registry;
import com.devit.mscore.exception.ApplicationRuntimeException;
import com.devit.mscore.exception.RegistryException;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZookeeperRegistry implements Registry {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZookeeperRegistry.class);

    private static final String ROOT_PATH = "/";

    private String name;

    private CuratorFramework client;

    private Map<String, String> cache;

    // TODO: deprecate the name parameter. Not used anymore.
    public ZookeeperRegistry(String name, CuratorFramework client) {
        this.name = name;
        this.client = client;
        this.cache = new HashMap<>();
    }

    public ZookeeperRegistry(ApplicationContext context, String name, CuratorFramework client) throws RegistryException {
        this(name, client);
        init(context);
    }

    public void init(ApplicationContext context) throws RegistryException {
        open();
        load(context, ROOT_PATH);
    }

    private void load(ApplicationContext context, String path) throws RegistryException {
        // get will put the value into cache
        get(context, path);

        loadChildren(context, path);
    }

    private void loadChildren(ApplicationContext context, String parentPath) throws RegistryException {
        try {
            var children = this.client.getChildren().forPath(parentPath);
            for (var child : children) {
                var format = parentPath.equals(ROOT_PATH) ? "%s%s" : "%s/%s";
                var key = String.format(format, parentPath, child);
                load(context, key);
            }
        } catch (Exception ex) {
            throw new RegistryException(ex);
        }
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void add(ApplicationContext context, String key, String value) throws RegistryException {
        try {
            var stat = this.client.checkExists().forPath(key);
            if (stat == null) {
                this.client.create().creatingParentsIfNeeded().forPath(key, value.getBytes());
            } else {
                this.client.setData().forPath(key, value.getBytes());
            }

            clearCache(getCacheKey(key));
        } catch (Exception ex) {
            throw new RegistryException("Cannot create register.", ex);
        }
    }

    private void clearCache(String cacheKey) {
        this.cache.remove(cacheKey);
    }

    static String getCacheKey(String key) {
        if (key.equals(ROOT_PATH)) {
            return "root";
        }

        var cacheKey = key.substring(1); // remove leading /
        return cacheKey.replace("/", ".");
    }

    @Override
    public String get(ApplicationContext context, String registryKey) throws RegistryException {
        var cacheKey = getCacheKey(registryKey);

        this.cache.computeIfAbsent(cacheKey, key -> {
            try {
                var stat = this.client.checkExists().forPath(registryKey);
                if (stat == null) {
                    LOGGER.warn("BreadcrumbdId: {}. No configuration value for key: {}.", context.getBreadcrumbId(), registryKey);
                    return null;
                }
                var value = this.client.getData().forPath(registryKey);
                return value != null ? new String(value) : null;
            } catch (Exception ex) {
                throw new ApplicationRuntimeException(new RegistryException("Cannot get register.", ex));
            }
        });

        return this.cache.get(cacheKey);
    }

    @Override
    public Map<String, String> all(ApplicationContext context) {
        return this.cache;
    }

    @Override
    public void open() {
        if (!isClientOpened()) {
            this.client.start();
        }
    }

    private boolean isClientOpened() {
        return this.client.getState().equals(CuratorFrameworkState.STARTED);
    }

    @Override
    public void close() {
        this.client.close();
    }

    @Override
    public List<String> values(ApplicationContext context) throws RegistryException {
        return new ArrayList<>(this.cache.values());
    }

    @Override
    public List<String> keys(ApplicationContext context) throws RegistryException {
        return new ArrayList<>(this.cache.keySet());
    }
}
