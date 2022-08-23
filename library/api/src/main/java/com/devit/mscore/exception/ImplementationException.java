package com.devit.mscore.exception;

public class ImplementationException extends ApplicationException {

  private static final long serialVersionUID = 1L;

  private static final String DEFAULT_MESSAGE = "Method is not available.";

  public ImplementationException() {
    this(DEFAULT_MESSAGE);
  }

  public ImplementationException(String message) {
    super(message);
  }

  public ImplementationException(Throwable cause) {
    super(cause);
  }

  public ImplementationException(String message, Throwable cause) {
    super(message, cause);
  }

}