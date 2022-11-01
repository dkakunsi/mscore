package com.devit.mscore.registry;

import com.devit.mscore.Logger;
import com.devit.mscore.Registry;
import com.devit.mscore.exception.RegistryException;
import com.devit.mscore.logging.ApplicationLogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MemoryRegistry implements Registry {

  private static final Logger LOG = ApplicationLogger.getLogger(MemoryRegistry.class);

  private String name;

  private Map<String, String> register;

  public MemoryRegistry(String name) {
    this(name, new HashMap<>());
  }

  public MemoryRegistry(String name, Map<String, String> register) {
    this.name = name;
    this.register = new HashMap<>(register);
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public void add(String key, String value) throws RegistryException {
    this.register.put(key, value);
    LOG.debug("Object '{}' is added to memory register", key);
  }

  @Override
  public Map<String, String> all() throws RegistryException {
    LOG.debug("Retrieving all value from memory register");
    return new HashMap<>(this.register);
  }

  @Override
  public List<String> values() throws RegistryException {
    return new ArrayList<>(this.register.values());
  }

  @Override
  public List<String> keys() throws RegistryException {
    return new ArrayList<>(this.register.keySet());
  }

  @Override
  public String get(String key) throws RegistryException {
    LOG.debug("Retrieving data with key '{}'", key);
    return this.register.get(key);
  }

  @Override
  public void open() {
    // no need to open a memory
    throw new UnsupportedOperationException();
  }

  @Override
  public void close() {
    // no need to close a memory
    throw new UnsupportedOperationException();
  }
}
