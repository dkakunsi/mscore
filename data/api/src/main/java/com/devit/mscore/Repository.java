package com.devit.mscore;

import com.devit.mscore.exception.DataException;
import com.devit.mscore.exception.ImplementationException;

import java.util.List;
import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Repository to connect to persistence storage.
 *
 * @author dkakunsi
 */
public interface Repository extends Cloneable {

  /**
   * Save data to persistence storage.
   *
   * @param json to be saved.
   * @return persisted object.
   * @throws DataException invalid data is provided.
   */
  default JSONObject save(JSONObject json) throws DataException {
    throw new DataException(new ImplementationException());
  }

  /**
   * Delete data base on it's id.
   *
   * @param id object id.
   * @throws DataException invalid data is provided.
   */
  default void delete(String id) throws DataException {
    throw new DataException(new ImplementationException());
  }

  /**
   * Find object by it's id.
   *
   * @param id data id.
   * @return the found data.
   * @throws DataException invalid data is provided.
   */
  default Optional<JSONObject> find(String id) throws DataException {
    throw new DataException(new ImplementationException());
  }

  /**
   * Find data by the given ids.
   *
   * @param ids object ids.
   * @return the found data.
   * @throws DataException invalid data is provided.
   */
  default Optional<JSONArray> find(List<String> ids) throws DataException {
    throw new DataException(new ImplementationException());
  }

  /**
   * Find data base on the specified field with value key.
   *
   * @param field the field to be searched.
   * @param key   the value to filter objects.
   * @return data matching search criteria.
   * @throws DataException invalid data is provided.
   */
  default Optional<JSONArray> find(String field, Object key) throws DataException {
    throw new DataException(new ImplementationException());
  }

  /**
   * Load all data from database.
   *
   * @return all data in database.
   * @throws DataException invalid data is provided.
   */
  default Optional<JSONArray> all() throws DataException {
    throw new DataException(new ImplementationException());
  }

  Object clone() throws CloneNotSupportedException;
}
