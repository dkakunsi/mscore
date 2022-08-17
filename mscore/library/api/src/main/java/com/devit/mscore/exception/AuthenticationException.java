package com.devit.mscore.exception;

/**
 * AuthenticationException
 */
public class AuthenticationException extends ApplicationException {

  private static final long serialVersionUID = 1L;

  public AuthenticationException(String message) {
    super(message);
  }

  public AuthenticationException(Throwable cause) {
    super(cause);
  }

  public AuthenticationException(String message, Throwable cause) {
    super(message, cause);
  }
}
