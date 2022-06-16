package com.devit.mscore.exception;

/**
 * TransformationException
 */
public class TransformationException extends ApplicationException {

    private static final long serialVersionUID = 1L;

    public TransformationException(String message) {
        super(message);
    }

    public TransformationException(Throwable cause) {
        super(cause);
    }

    public TransformationException(String message, Throwable cause) {
        super(message, cause);
    }
}
