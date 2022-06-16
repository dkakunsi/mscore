package com.devit.mscore;

import com.devit.mscore.exception.ApplicationException;

/**
 * <p>
 * Interface for object that can be started and stopped.
 * </p>
 * <p>
 * This usually works for server or listener
 * </p>
 * 
 * @author dkakunsi
 */
public interface Starter {

    /**
     * Start the object.
     * 
     * @throws ApplicationException failure in system start.
     */
    void start() throws ApplicationException;

    /**
     * Stop the object.
     */
    void stop();
}
