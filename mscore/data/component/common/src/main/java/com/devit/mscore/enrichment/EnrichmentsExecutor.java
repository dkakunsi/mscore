package com.devit.mscore.enrichment;

import static com.devit.mscore.util.AttributeConstants.getDomain;
import static com.devit.mscore.util.AttributeConstants.getId;
import static com.devit.mscore.util.Utils.ALL;

import java.util.HashMap;
import java.util.Map;

import com.devit.mscore.ApplicationContext;
import com.devit.mscore.Executor;
import com.devit.mscore.exception.EnrichmentException;
import com.devit.mscore.Enrichment;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Mediate the execution of {@code enrichments}. It will use it on demand.
 * </p>
 * 
 * @author dkakunsi
 */
public final class EnrichmentsExecutor implements Executor<Enrichment> {

    private static final Logger LOG = LoggerFactory.getLogger(EnrichmentsExecutor.class);

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
    public void execute(ApplicationContext context, JSONObject json) {
        var domain = getDomain(json);
        enrich(context, this.enrichments.get(ALL), json);
        if (StringUtils.isEmpty(domain)) {
            LOG.warn("BreadcrumbId: {}. Fail to enrich domain-specific attributes of {}. Domain is not provided.", context.getBreadcrumbId(), json);
            return;
        }
        enrich(context, this.enrichments.get(domain), json);
    }

    private static void enrich(ApplicationContext context, Map<String, Enrichment> enrichments, JSONObject json) {
        if (enrichments == null || json == null) {
            return;
        }

        enrichments.forEach((key, enrichment) -> {
            if (json.has(key)) {
                var id = getId(json);
                var domain = getDomain(json);
                try {
                    LOG.info("BreadcrumbId: {}. Enriching attribute {} of {} for {} domain", context.getBreadcrumbId(), key, id, domain);
                    enrichment.enrich(context, json);
                } catch (EnrichmentException ex) {
                    LOG.warn("BreadcrumbId: {}. Cannot enrich attribute {} of {} for {} domain", context.getBreadcrumbId(), key, id, domain, ex);
                }
            } else {
                LOG.warn("BreadcrumbId: {}. Cannot enrich non-existing attribute {}", context.getBreadcrumbId(), key);
            }
        });
    }
}
