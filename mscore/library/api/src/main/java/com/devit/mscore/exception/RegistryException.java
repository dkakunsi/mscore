package com.devit.mscore.exception;

public class RegistryException extends ApplicationException {

  private static final long serialVersionUID = 1L;

  public RegistryException(String message) {
    super(message);
  }

  public RegistryException(Throwable cause) {
    super(cause);
  }

  public RegistryException(String message, Throwable cause) {
    super(message, cause);
  }
}
