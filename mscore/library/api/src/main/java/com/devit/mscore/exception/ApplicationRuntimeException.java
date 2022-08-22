package com.devit.mscore.exception;

/**
 * Application-wide unchecked exception.
 *
 * @author dkakunsi
 */
public class ApplicationRuntimeException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public ApplicationRuntimeException(ApplicationException ex) {
    super(ex);
  }

  public ApplicationRuntimeException(Throwable ex) {
    super(ex);
  }

  public ApplicationRuntimeException(String message) {
    super(message);
  }
}
