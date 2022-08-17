package com.devit.mscore.exception;

/**
 * WebClientException
 */
public class WebClientException extends ApplicationException {

  private static final long serialVersionUID = 1L;

  public WebClientException(String message) {
    super(message);
  }

  public WebClientException(Throwable cause) {
    super(cause);
  }

  public WebClientException(String message, Throwable cause) {
    super(message, cause);
  }
}
