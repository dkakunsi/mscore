package com.devit.mscore;

import com.devit.mscore.exception.SynchronizationException;

/**
 * <p>
 * Root class for synchronization process. Sync object that meet search
 * attribute using the given <code>synchronizer</code>
 * </p>
 * 
 * @author dkakunsi
 */
public abstract class Synchronization {

    protected String referenceDomain;

    protected String referenceAttribute;

    protected Synchronizer synchronizer;

    protected Synchronization(Synchronizer synchronizer, String referenceDomain, String referenceAttribute) {
        this.referenceDomain = referenceDomain;
        this.referenceAttribute = referenceAttribute;
        this.synchronizer = synchronizer;
    }

    /**
     * <p>
     * Get the reference domain that triggers synchronization.
     * </p>
     * 
     * @return reference domain.
     */
    public String getReferenceDomain() {
        return this.referenceDomain;
    }

    /**
     * <p>
     * Get the reference attribute that triggers synchronization.
     * </p>
     * 
     * @return reference attribute.
     */
    public String getReferenceAttribute() {
        return this.referenceAttribute;
    }

    /**
     * Execute the sync process.
     * 
     * @param context     application context.
     * @param referenceId to synchronize.
     */
    public abstract void synchronize(ApplicationContext context, String referenceId) throws SynchronizationException;

    protected String getSearchAttribute() {
        return String.format("%s.id", this.referenceAttribute);
    }

    protected Synchronizer getSynchronizer() {
        return this.synchronizer;
    }
}
