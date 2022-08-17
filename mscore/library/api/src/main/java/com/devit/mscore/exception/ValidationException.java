package com.devit.mscore.exception;

/**
 * ValidationException
 */
public class ValidationException extends ApplicationException {

  private static final long serialVersionUID = 1L;

  public ValidationException(String message) {
    super(message);
  }

  public ValidationException(Throwable cause) {
    super(cause);
  }

  public ValidationException(String message, Throwable cause) {
    super(message, cause);
  }
}
