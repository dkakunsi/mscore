package com.devit.mscore.exception;

/**
 * Root exception for data issue.
 * 
 * @author dkakunsi
 */
public class DataNotFoundException extends DataException {

    private static final long serialVersionUID = 1L;

    public DataNotFoundException(String message) {
        super(message);
    }

    public DataNotFoundException(Throwable cause) {
        super(cause);
    }

    public DataNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
