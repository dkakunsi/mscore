package com.devit.mscore;

import com.devit.mscore.exception.ApplicationException;
import com.devit.mscore.exception.ImplementationException;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Object to handle business process of a service and communication to external
 * system.
 *
 * @author dkakunsi
 */
public interface Service  extends Cloneable {

  /**
   *
   * @return domain name.
   */
  String getDomain();

  /**
   * Save an object into repository.
   *
   * @param json to be saved.
   * @return id.
   * @throws ApplicationException application rule is not met.
   */
  default String save(JSONObject json) throws ApplicationException {
    throw new ImplementationException();
  }

  /**
   * Delete an object from repository.
   *
   * @param id to delete.
   * @throws ApplicationException application rule is not met.
   */
  default void delete(String id) throws ApplicationException {
    throw new ImplementationException();
  }

  /**
   * Find object by it's id.
   *
   * @param id to find.
   * @return found object.
   * @throws ApplicationException application rule is not met.
   */
  default JSONObject find(String id) throws ApplicationException {
    throw new ImplementationException();
  }

  /**
   * Find object by it's code.
   *
   * @param code to find.
   * @return found objects.
   * @throws ApplicationException application rule is not met.
   */
  default JSONObject findByCode(String code) throws ApplicationException {
    throw new ImplementationException();
  }

  /**
   * Find objects with id in ids.
   *
   * @param ids to find.
   * @return found objects.
   * @throws ApplicationException application rule is not met.
   */
  default JSONArray find(List<String> ids) throws ApplicationException {
    throw new ImplementationException();
  }

  /**
   * Load all available object.
   *
   * @return all object.
   * @throws ApplicationException
   */
  default JSONArray all() throws ApplicationException {
    throw new ImplementationException();
  }

  /**
   * Search data based on {@code query}.
   *
   * @param query to search.
   * @return data matching the {@code query}.
   * @throws ApplicationException
   */
  default JSONArray search(JSONObject query) throws ApplicationException {
    throw new ImplementationException();
  }

  Object clone() throws CloneNotSupportedException;
}