package com.devit.mscore.enrichment;

import java.util.Map;
import java.util.Optional;

import com.devit.mscore.Enrichment;
import com.devit.mscore.Index;
import com.devit.mscore.Logger;
import com.devit.mscore.exception.DataException;
import com.devit.mscore.exception.IndexingException;
import com.devit.mscore.logging.ApplicationLogger;

import org.json.JSONObject;

/**
 * Enrich object using data in Elasticsearch.
 * 
 * <p>
 * <i>Only use 1-level enrichment. Since data in ES are already enriched.</i>
 * </p>
 * 
 * @author dkakunsi
 */
public class IndexEnrichment extends Enrichment {

    private static final Logger LOG = ApplicationLogger.getLogger(IndexEnrichment.class);

    private final Map<String, Index> indeces;

    public IndexEnrichment(Map<String, Index> indeces, String domain, String attribute) {
        super(domain, attribute);
        this.indeces = indeces;
    }

    @Override
    protected Optional<JSONObject> loadFromDataStore(String domain, String id)
            throws DataException {

        try {
            var index = this.indeces.get(domain);
            if (index != null) {
                return index.get(id);
            } else {
                LOG.warn("Cannot enrich: {}. No index available for domain: {}.", id, domain);
                return Optional.empty();
            }
        } catch (IndexingException ex) {
            LOG.error("Cannot load '{}' from '{}' domain", id, domain);
            throw new DataException("Cannot load data from index.", ex);
        }
    }
}
