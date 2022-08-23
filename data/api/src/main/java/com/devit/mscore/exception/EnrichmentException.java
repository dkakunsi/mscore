package com.devit.mscore.exception;

public class EnrichmentException extends ApplicationException {

  private static final long serialVersionUID = 1L;

  public EnrichmentException(String message) {
    super(message);
  }

  public EnrichmentException(Throwable cause) {
    super(cause);
  }

  public EnrichmentException(String message, Throwable cause) {
    super(message, cause);
  }
}
