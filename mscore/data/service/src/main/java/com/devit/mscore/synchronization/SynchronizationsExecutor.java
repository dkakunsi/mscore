package com.devit.mscore.synchronization;

import static com.devit.mscore.util.AttributeConstants.getDomain;
import static com.devit.mscore.util.AttributeConstants.getId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.devit.mscore.ApplicationContext;
import com.devit.mscore.Executor;
import com.devit.mscore.Schema;
import com.devit.mscore.Synchronization;
import com.devit.mscore.Synchronizer;
import com.devit.mscore.exception.SynchronizationException;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Mediate the execution of {@code synchronizations}. It will use it on demand.
 * </p>
 * 
 * @author dkakunsi
 */
public final class SynchronizationsExecutor implements Executor<Synchronization> {

    private static final Logger LOG = LoggerFactory.getLogger(SynchronizationsExecutor.class);

    private Map<String, List<Synchronization>> synchronizations;

    public SynchronizationsExecutor() {
        this.synchronizations = new HashMap<>();
    }

    public Map<String, List<Synchronization>> getSynchronizations() {
        return this.synchronizations;
    }

    /**
     * Add synchronizations generated from the synchronizer.
     * 
     * @param synchronizer service.
     */
    public void add(Synchronizer synchronizer) {
        var resource = synchronizer.getSchema();
        if (!(resource instanceof Schema)) {
            LOG.info("Cannot sync data because schema is not found.");
            return;
        }

        var schema = (Schema) resource;
        schema.getReferences().forEach((referenceAttribute, referenceDomains) -> {
            referenceDomains.forEach(referenceDomain -> add(new DefaultSynchronization(synchronizer, referenceDomain, referenceAttribute)));
        });
    }

    @Override
    public void add(Synchronization synchronization) {
        var domain = synchronization.getReferenceDomain();
        this.synchronizations.computeIfAbsent(domain, key -> new ArrayList<>());
        this.synchronizations.get(domain).add(synchronization);
    }

    /**
     * Execute synhronizations associated with the changed {@code json} object.
     * 
     * @param context of the request
     * @param json object that changes and trigger the synchronization.
     */
    @Override
    public void execute(ApplicationContext context, JSONObject json) {
        var referenceDomain = getDomain(json);
        var referenceId = getId(json);
        if (StringUtils.isEmpty(referenceDomain)) {
            LOG.warn("BreadcrumbId: {}. Fail to synchronize object {}. Domain is not provided.", context.getBreadcrumbId(), referenceId);
            return;
        }
        synchronize(context, this.synchronizations.get(referenceDomain), referenceId);
    }

    private static void synchronize(ApplicationContext context, List<Synchronization> synchronizations, String referenceId) {
        if (synchronizations == null || StringUtils.isBlank(referenceId)) {
            return;
        }

        synchronizations.forEach(s -> {
            try {
                s.synchronize(context, referenceId);
            } catch (SynchronizationException ex) {
                LOG.error("BreadcrumbId: {}. Cannot synchronize object {}", context.getBreadcrumbId(), referenceId, ex);
            }
        });
    }
}
