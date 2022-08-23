package com.devit.mscore.exception;

public class ProcessException extends ApplicationException {

  private static final long serialVersionUID = 1L;

  public ProcessException(String message) {
    super(message);
  }

  public ProcessException(Throwable cause) {
    super(cause);
  }

  public ProcessException(String message, Throwable cause) {
    super(message, cause);
  }
}