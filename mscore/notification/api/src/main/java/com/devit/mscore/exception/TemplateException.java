package com.devit.mscore.exception;

public class TemplateException extends ApplicationException {

    private static final long serialVersionUID = 1L;

    public TemplateException(String message) {
        super(message);
    }

    public TemplateException(Throwable cause) {
        super(cause);
    }

    public TemplateException(String message, Throwable cause) {
        super(message, cause);
    }
}
