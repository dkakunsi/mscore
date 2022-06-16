package com.devit.mscore.exception;

/**
 * AuthenticationException
 */
public class IndexingException extends ApplicationException {

    private static final long serialVersionUID = 1L;

    public IndexingException(String message) {
        super(message);
    }

    public IndexingException(Throwable cause) {
        super(cause);
    }

    public IndexingException(String message, Throwable cause) {
        super(message, cause);
    }
}
