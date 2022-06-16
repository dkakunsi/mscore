package com.devit.mscore.exception;

public class ConfigException extends ApplicationException {

    private static final long serialVersionUID = 1L;

    public ConfigException(String message) {
      super(message);
    }

    public ConfigException(Throwable cause) {
      super(cause);
    }

    public ConfigException(String message, Throwable cause) {
      super(message, cause);
    }
}