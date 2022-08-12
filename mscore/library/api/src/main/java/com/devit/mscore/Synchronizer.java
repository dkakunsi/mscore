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
    public Resource getSchema();

    /**
     * <p>
     * Synchronize object with the given <code>id</code>.
     * </p>
     * 
     * @param id      id of object to sync.
     * 
     * @throws SynchronizationException
     */
    void synchronize(String id) throws SynchronizationException;

    /**
     * <p>
     * Synchronize all object related to <code>searchAttribute</code> with the given
     * <code>value</code>.
     * </p>
     * 
     * @param searchAttribute attribute to meet.
     * @param value           value of attribute to search.
     * 
     * @throws SynchronizationException
     */
    void synchronize(String searchAttribute, String value) throws SynchronizationException;

    /**
     * <p>
     * Synchronize all object in database.
     * </p>
     * 
     * @throws SynchronizationException
     */
    void synchronize() throws SynchronizationException;
}
