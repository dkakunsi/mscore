package com.devit.mscore;

import java.util.List;

import com.devit.mscore.exception.ApplicationException;
import com.devit.mscore.exception.ImplementationException;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Object to handle business process of a service and communication to external
 * system.
 * 
 * @author dkakunsi
 */
public interface Service {

    /**
     * 
     * @return domain name.
     */
    String getDomain();

    /**
     * Save an object into repository.
     * 
     * @param context application context.
     * @param json    to be saved.
     * @return id.
     * @throws ApplicationException application rule is not met.
     */
    default String save(ApplicationContext context, JSONObject json) throws ApplicationException {
        throw new ImplementationException();
    }

    /**
     * Delete an object from repository.
     * 
     * @param context application context.
     * @param id      to delete.
     * @throws ApplicationException application rule is not met.
     */
    default void delete(ApplicationContext context, String id) throws ApplicationException {
        throw new ImplementationException();
    }

    /**
     * Find object by it's id.
     * 
     * @param context application context.
     * @param id      to find.
     * @return found object.
     * @throws ApplicationException application rule is not met.
     */
    default JSONObject find(ApplicationContext context, String id) throws ApplicationException {
        throw new ImplementationException();
    }

    /**
     * Find object by it's code.
     * 
     * @param context application context.
     * @param code    to find.
     * @return found objects.
     * @throws ApplicationException application rule is not met.
     */
    default JSONObject findByCode(ApplicationContext context, String code) throws ApplicationException {
        throw new ImplementationException();
    }

    /**
     * Find objects with id in ids.
     * 
     * @param context application context.
     * @param ids     to find.
     * @return found objects.
     * @throws ApplicationException application rule is not met.
     */
    default JSONArray find(ApplicationContext context, List<String> ids) throws ApplicationException {
        throw new ImplementationException();
    }

    /**
     * Load all available object.
     * 
     * @param context of the request.
     * @return all object.
     * @throws ApplicationException
     */
    default JSONArray all(ApplicationContext context) throws ApplicationException {
        throw new ImplementationException();
    }

    /**
     * Search data based on {@code query}.
     * 
     * @param context of the request.
     * @param query   to search.
     * @return data matching the {@code query}.
     * @throws ApplicationException
     */
    default JSONArray search(ApplicationContext context, JSONObject query) throws ApplicationException {
        throw new ImplementationException();
    }
}