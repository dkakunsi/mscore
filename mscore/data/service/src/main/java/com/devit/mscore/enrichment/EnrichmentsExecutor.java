package com.devit.mscore.enrichment;

import static com.devit.mscore.util.AttributeConstants.getDomain;
import static com.devit.mscore.util.AttributeConstants.getId;
import static com.devit.mscore.util.Utils.ALL;

import java.util.HashMap;
import java.util.Map;

import com.devit.mscore.Executor;
import com.devit.mscore.Logger;
import com.devit.mscore.exception.EnrichmentException;
import com.devit.mscore.logging.ApplicationLogger;
import com.devit.mscore.Enrichment;

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
    this.enrichments = new HashMap<>();
  }

  @Override
  public void add(Enrichment enrichment) {
    var domain = enrichment.getDomain();
    this.enrichments.computeIfAbsent(domain, key -> new HashMap<>());
    this.enrichments.get(domain).put(enrichment.getAttribute(), enrichment);
  }

  @Override
  public void execute(JSONObject json) {
    var domain = getDomain(json);
    enrich(this.enrichments.get(ALL), json);
    if (StringUtils.isEmpty(domain)) {
      LOG.warn("Fail to enrich domain-specific attributes of {}. Domain is not provided.", json);
      return;
    }
    enrich(this.enrichments.get(domain), json);
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
          LOG.info("Enriching attribute {} of {} for {} domain", key, id, domain);
          enrichment.enrich(json);
        } catch (EnrichmentException ex) {
          LOG.warn("Cannot enrich attribute {} of {} for {} domain", key, id, domain, ex);
        }
      } else {
        LOG.warn("Cannot enrich non-existing attribute {}", key);
      }
    });
  }
}
