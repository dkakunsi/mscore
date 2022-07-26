package com.devit.mscore.data.enrichment;

import static com.devit.mscore.util.AttributeConstants.getDomain;
import static com.devit.mscore.util.AttributeConstants.getId;
import static com.devit.mscore.util.Constants.ALL;

import com.devit.mscore.Enrichment;
import com.devit.mscore.Executor;
import com.devit.mscore.Logger;
import com.devit.mscore.exception.EnrichmentException;
import com.devit.mscore.logging.ApplicationLogger;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

/**
 * <p>
 * Mediate the execution of {@code enrichments}. It will use it on demand.
 * </p>
 *
 * @author dkakunsi
 */
public final class EnrichmentsExecutor implements Executor<Enrichment> {

  private static final Logger LOG = ApplicationLogger.getLogger(EnrichmentsExecutor.class);

  private Map<String, Map<String, Enrichment>> enrichments;

  public EnrichmentsExecutor() {
    enrichments = new HashMap<>();
  }

  @Override
  public void add(Enrichment enrichment) {
    var domain = enrichment.getDomain();
    enrichments.computeIfAbsent(domain, key -> new HashMap<>());
    enrichments.get(domain).put(enrichment.getAttribute(), enrichment);
  }

  @Override
  public void execute(JSONObject json) {
    var domain = getDomain(json);
    enrich(enrichments.get(ALL), json);
    if (StringUtils.isEmpty(domain)) {
      LOG.warn("Fail to enrich domain-specific attributes of '{}'. Domain is not provided", json);
      return;
    }
    enrich(enrichments.get(domain), json);
  }

  private static void enrich(Map<String, Enrichment> enrichments, JSONObject json) {
    if (enrichments == null || json == null) {
      return;
    }

    enrichments.forEach((key, enrichment) -> {
      if (json.has(key)) {
        var id = getId(json);
        var domain = getDomain(json);
        try {
          LOG.info("Enriching attribute '{}' of object '{}' in domain '{}'", key, id, domain);
          enrichment.enrich(json);
        } catch (EnrichmentException ex) {
          LOG.warn("Cannot enrich attribute '{}' of object '{}' in domain '{}'", key, id, domain, ex);
        }
      } else {
        LOG.warn("Cannot enrich non-existing attribute '{}'", key);
      }
    });
  }
}
