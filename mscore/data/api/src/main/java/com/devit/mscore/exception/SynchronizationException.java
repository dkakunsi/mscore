package com.devit.mscore.exception;

public class SynchronizationException extends ApplicationException {

    private static final long serialVersionUID = 1L;

    public SynchronizationException(String message) {
        super(message);
    }

    public SynchronizationException(Throwable cause) {
        super(cause);
    }

    public SynchronizationException(String message, Throwable cause) {
        super(message, cause);
    }
}
