package com.devit.mscore;

import com.devit.mscore.exception.SynchronizationException;

/**
 * <p>
 * Interface for synchronization object of a specific domain.
 * </p>
 * 
 * <p>
 * Steps for synchronization are:
 * <ol>
 * <li>Load related objects from the given <code>repository</code>.</li>
 * <li>Enrich it using <code>enricher</code>.</li>
 * <li>Publish it to the system using the given <code>publisher</code>.</li>
 * </ol>
 * </p>
 * 
 * @author dkakunsi
 * 
 */
public interface Synchronizer {

    /**
     * Get the schema of the domain to synchronize.
     * 
     * @return schema of domain.
     */
    public Schema getSchema();

    /**
     * <p>
     * Synchronize object with the given <code>id</code>.
     * </p>
     * 
     * @param context application context.
     * @param id      id of object to sync.
     * 
     * @throws SynchronizationException
     */
    void synchronize(ApplicationContext context, String id) throws SynchronizationException;

    /**
     * <p>
     * Synchronize all object related to <code>searchAttribute</code> with the given
     * <code>value</code>.
     * </p>
     * 
     * @param context         application context.
     * @param searchAttribute attribute to meet.
     * @param value           value of attribute to search.
     * 
     * @throws SynchronizationException
     */
    void synchronize(ApplicationContext context, String searchAttribute, String value) throws SynchronizationException;

    /**
     * <p>
     * Synchronize all object in database.
     * </p>
     * 
     * @param context application context.
     * 
     * @throws SynchronizationException
     */
    void synchronize(ApplicationContext context) throws SynchronizationException;
}