package com.devit.mscore;

import static com.devit.mscore.util.AttributeConstants.getCode;
import static com.devit.mscore.util.AttributeConstants.getId;
import static com.devit.mscore.util.AttributeConstants.hasDomain;
import static com.devit.mscore.util.AttributeConstants.hasId;
import static com.devit.mscore.util.Constants.ALL;
import static com.devit.mscore.util.JsonUtils.hasValue;

import com.devit.mscore.exception.DataException;
import com.devit.mscore.exception.EnrichmentException;
import com.devit.mscore.logging.ApplicationLogger;
import com.devit.mscore.util.AttributeConstants;
import com.devit.mscore.util.JsonUtils;

import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * <p>
 * Enrich any reference of object loaded from data source. The reference object
 * could be in the same or different data source.
 * </p>
 *
 * <p>
 * The implementor of this interface should specified the logic in loading data
 * from it's data source.
 * </p>
 *
 * @author dkakunsi
 */
public abstract class Enrichment {

  private static final Logger LOGGER = ApplicationLogger.getLogger(Enrichment.class);

  protected final String domain;

  protected final String attribute;

  protected Enrichment(String attribute) {
    this(ALL, attribute);
  }

  protected Enrichment(String domain, String attribute) {
    this.domain = domain;
    this.attribute = attribute;
  }

  /**
   *
   * @return the domain this enrichment applies to.
   */
  public String getDomain() {
    return domain;
  }

  /**
   *
   * @return the attribute this enrichment applies to.
   */
  public String getAttribute() {
    return attribute;
  }

  /**
   * Check if the given json has ID and DOMAIN value.
   *
   * @param json to validate.
   * @return true if has ID and DOMAIN value, otherwise false.
   */
  protected boolean isValid(JSONObject json) {
    return hasId(json) && hasDomain(json);
  }

  /**
   * Enrich the given json object.
   *
   * @param json object to enrich.
   * @throws EnrichmentException error in enrichment
   */
  public void enrich(JSONObject json) throws EnrichmentException {
    LOGGER.debug("Enriching attribute '{}' of object '{}'", attribute, getId(json));

    if (!hasValue(attribute, json)) {
      LOGGER.warn("Attribut '{}' cannot be enriched since it is not in json object", attribute);
      return;
    }

    var value = json.get(attribute);
    if (value instanceof JSONArray) {
      for (var v : (JSONArray) value) {
        enrichReference((JSONObject) v);
      }
    } else if (value instanceof JSONObject) {
      enrichReference((JSONObject) value);
    } else {
      LOGGER.error("Trying to enrich non JSON value is not allowed");
      throw new EnrichmentException("Cannot enrich object. Only JSONObject or JSONArray is allowed");
    }
  }

  protected void enrichReference(JSONObject value) throws EnrichmentException {
    if (!isValid(value)) {
      LOGGER.warn("Attribut '{}' cannot be enriched. No id and/or domain for code '{}'", attribute, getCode(value));
      return;
    }

    try {
      var refDomain = AttributeConstants.getDomain(value);
      var refId = getId(value);
      var loadedObject = retriableLoad(refDomain, refId);
      if (loadedObject.isPresent()) {
        JsonUtils.copy(value, loadedObject.get());
      } else {
        LOGGER.info("No entity found for reference '{}' in index '{}'", refId, refDomain);
      }
    } catch (DataException | JSONException ex) {
      LOGGER.error("Cannot enrich object");
      throw new EnrichmentException("Cannot enrich object", ex);
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
    }
  }

  private Optional<JSONObject> retriableLoad(String refDomain, String refId)
      throws DataException, InterruptedException {
    var loadedObject = loadFromDataStore(refDomain, refId);
    var retryNumber = 0;
    while (loadedObject.isEmpty() && retryNumber < 3) {
      Thread.sleep(1000L);
      retryNumber++;
      LOGGER.info("Try: #{}. Load entity '{}' from domain '{}'", retryNumber, refId, refDomain);
      loadedObject = loadFromDataStore(refDomain, refId);
    }
    return loadedObject;
  }

  /**
   * Load object from data store.
   *
   * @param domain where the data are stored.
   * @param id     of the data.
   * @return complete data.
   * @throws DataException can't load data due to some reasons.
   */
  protected abstract Optional<JSONObject> loadFromDataStore(String domain, String id)
      throws DataException;
}
