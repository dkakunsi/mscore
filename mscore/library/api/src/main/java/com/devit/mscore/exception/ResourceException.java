package com.devit.mscore.exception;

/**
 * AuthenticationException
 */
public class ResourceException extends ApplicationException {

    private static final long serialVersionUID = 1L;

    public ResourceException(String message) {
        super(message);
    }

    public ResourceException(Throwable cause) {
        super(cause);
    }

    public ResourceException(String message, Throwable cause) {
        super(message, cause);
    }
}
