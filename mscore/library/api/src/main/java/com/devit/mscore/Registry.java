package com.devit.mscore;

import java.util.List;
import java.util.Map;

import com.devit.mscore.exception.RegistryException;

/**
 * Object to manage system registry.
 * 
 * @author dkakunsi
 */
public interface Registry {

    /**
     * Get registry name.
     * 
     * @return registry name.
     */
    String getName();

    /**
     * Add value to registry key
     * 
     * @param context application context.
     * @param key     registry key.
     * @param value   registry value
     * @throws RegistryException cannot register key and value.
     */
    void add(ApplicationContext context, String key, String value) throws RegistryException;

    /**
     * Retrieve registry with key.
     * 
     * @param context application context.
     * @param key     registry key.
     * @return registry value.
     * @throws RegistryException cannot manage registry data.
     */
    String get(ApplicationContext context, String key) throws RegistryException;

    /**
     * Retrieve all registry contents.
     * 
     * @param context of the request.
     * @return all registry contents.
     * @throws RegistryException cannot manage registry data.
     */
    Map<String, String> all(ApplicationContext context) throws RegistryException;

    /**
     * 
     * @param context of the request.
     * @return all values of the registry.
     * @throws RegistryException cannot get registry data.
     */
    List<String> values(ApplicationContext context) throws RegistryException;

    /**
     * 
     * @param context of the request.
     * @return all keys of the registry.
     * @throws RegistryException cannot get reistry data.
     */
    List<String> keys(ApplicationContext context) throws RegistryException;

    /**
     * Open connection to registry.
     */
    void open();

    /**
     * Close connection to registry.
     */
    void close();
}
