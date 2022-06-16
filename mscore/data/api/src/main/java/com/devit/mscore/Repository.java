package com.devit.mscore;

import java.util.List;
import java.util.Optional;

import com.devit.mscore.exception.DataException;
import com.devit.mscore.exception.ImplementationException;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Repository to connect to persistence storage.
 * 
 * @author dkakunsi
 */
public interface Repository {

    /**
     * Save data to persistence storage.
     * 
     * @param context application context.
     * @param json    to be saved.
     * @return persisted object.
     * @throws DataException invalid data is provided.
     */
    default JSONObject save(ApplicationContext context, JSONObject json) throws DataException {
        throw new DataException(new ImplementationException());
    }

    /**
     * Delete data base on it's id.
     * 
     * @param context application context.
     * @param id      object id.
     * @throws DataException invalid data is provided.
     */
    default void delete(ApplicationContext context, String id) throws DataException {
        throw new DataException(new ImplementationException());
    }

    /**
     * Find object by it's id.
     * 
     * @param context application context.
     * @param id      data id.
     * @return the found data.
     * @throws DataException invalid data is provided.
     */
    default Optional<JSONObject> find(ApplicationContext context, String id) throws DataException {
        throw new DataException(new ImplementationException());
    }

    /**
     * Find data by the given ids.
     * 
     * @param context application context.
     * @param ids     object ids.
     * @return the found data.
     * @throws DataException invalid data is provided.
     */
    default Optional<JSONArray> find(ApplicationContext context, List<String> ids) throws DataException {
        throw new DataException(new ImplementationException());
    }

    /**
     * Find data base on the specified field with value key.
     * 
     * @param context application context.
     * @param field   the field to be searched.
     * @param key     the value to filter objects.
     * @return data matching search criteria.
     * @throws DataException invalid data is provided.
     */
    default Optional<JSONArray> find(ApplicationContext context, String field, Object key) throws DataException {
        throw new DataException(new ImplementationException());
    }

    /**
     * Load all data from database.
     * 
     * @param context of the request.
     * @return all data in database.
     * @throws DataException invalid data is provided.
     */
    default Optional<JSONArray> all(ApplicationContext context) throws DataException {
        throw new DataException(new ImplementationException());
    }
}
