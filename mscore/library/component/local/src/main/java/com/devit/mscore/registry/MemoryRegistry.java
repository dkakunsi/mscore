package com.devit.mscore.registry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.devit.mscore.ApplicationContext;
import com.devit.mscore.Registry;
import com.devit.mscore.exception.RegistryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MemoryRegistry implements Registry {

    private static final Logger LOG = LoggerFactory.getLogger(MemoryRegistry.class);

    private String name;

    private Map<String, String> register;

    public MemoryRegistry(String name) {
        this.name = name;
        this.register = new HashMap<>();
    }

    public MemoryRegistry(String name, Map<String, String> register) {
        this.name = name;
        this.register = register;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void add(ApplicationContext context, String key, String value) throws RegistryException {
        this.register.put(key, value);
        LOG.debug("BreadcrumbId: {}. Object is added to memory register: {}", context.getBreadcrumbId(), value);
    }

    @Override
    public Map<String, String> all(ApplicationContext context) throws RegistryException {
        LOG.debug("BreadcrumbId: {}. Retrieving all value from memory register", context.getBreadcrumbId());
        return this.register;
    }

    @Override
    public List<String> values(ApplicationContext context) throws RegistryException {
        return new ArrayList<>(this.register.values());
    }

    @Override
    public List<String> keys(ApplicationContext context) throws RegistryException {
        return new ArrayList<>(this.register.keySet());
    }

    @Override
    public String get(ApplicationContext context, String key) throws RegistryException {
        LOG.debug("BreadcrumbId: {}. Retrieving value from memory register with key: {}", context.getBreadcrumbId(),
                key);
        return this.register.get(key);
    }

    @Override
    public void open() {
        // no need to open a memory
    }

    @Override
    public void close() {
        // no need to close a memory
    }
}
