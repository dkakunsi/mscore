package com.devit.mscore;

import static com.devit.mscore.util.AttributeConstants.getId;
import static com.devit.mscore.util.AttributeConstants.hasDomain;
import static com.devit.mscore.util.AttributeConstants.hasId;
import static com.devit.mscore.util.JsonUtils.hasValue;
import static com.devit.mscore.util.Utils.ALL;

import java.util.Optional;

import com.devit.mscore.exception.DataException;
import com.devit.mscore.exception.EnrichmentException;
import com.devit.mscore.logging.ApplicationLogger;
import com.devit.mscore.util.AttributeConstants;
import com.devit.mscore.util.JsonUtils;

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

    private static final Logger LOG = new ApplicationLogger(Enrichment.class);

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
        return this.domain;
    }

    /**
     * 
     * @return the attribute this enrichment applies to.
     */
    public String getAttribute() {
        return this.attribute;
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
        LOG.debug("Enriching {} of object {}", this.attribute, json);

        if (!hasValue(this.attribute, json)) {
            LOG.warn("Attribut {} cannot be enriched since it is not in json object.", this.attribute);
            return;
        }

        var value = json.get(this.attribute);
        if (value instanceof JSONArray) {
            for (var v : (JSONArray) value) {
                enrichReference((JSONObject) v);
            }
        } else if (value instanceof JSONObject) {
            enrichReference((JSONObject) value);
        } else {
            LOG.error("Trying enriching non JSON value is not allowed.");
            throw new EnrichmentException("Cannot enrich object. Only JSONObject or JSONArray is allowed");
        }
    }

    protected void enrichReference(JSONObject value) throws EnrichmentException {
        if (!isValid(value)) {
            LOG.warn("Attribut {} cannot be enriched. No id and/or domain: {}.", this.attribute, value);
            return;
        }

        try {
            var refDomain = AttributeConstants.getDomain(value);
            var refId = getId(value);
            var loadedObject = retriableLoad(refDomain, refId);
            if (loadedObject.isPresent()) {
                JsonUtils.copy(value, loadedObject.get());
            } else {
                LOG.info("No entity found for reference {} in index {}",
                        refId, refDomain);
            }
        } catch (DataException | JSONException | InterruptedException ex) {
            LOG.error("Cannot enrich object.");
            throw new EnrichmentException("Cannot enrich object.", ex);
        }
    }

    private Optional<JSONObject> retriableLoad(String refDomain, String refId)
            throws DataException, InterruptedException {
        var loadedObject = loadFromDataStore(refDomain, refId);
        var retried = 0;
        while (loadedObject.isEmpty() && retried < 3) {
            Thread.sleep(1000L);
            retried++;
            LOG.info("Retry: {}. Load entity {} from {}.", retried, refId, refDomain);
            loadedObject = loadFromDataStore(refDomain, refId);
        }
        return loadedObject;
    }

    /**
     * Load object from data store.
     * 
     * @param domain  where the data are stored.
     * @param id      of the data.
     * @return complete data.
     * @throws DataException can't load data due to some reasons.
     */
    protected abstract Optional<JSONObject> loadFromDataStore(String domain, String id)
            throws DataException;
}
