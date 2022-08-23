package com.devit.mscore.exception;

/**
 * Root exception for data issue.
 *
 * @author dkakunsi
 */
public class DataException extends ApplicationException {

  private static final long serialVersionUID = 1L;

  public DataException(String message) {
    super(message);
  }

  public DataException(Throwable cause) {
    super(cause);
  }

  public DataException(String message, Throwable cause) {
    super(message, cause);
  }
}
