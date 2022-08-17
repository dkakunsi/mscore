package com.devit.mscore.exception;

/**
 * Root exception for data issue.
 * 
 * @author dkakunsi
 */
public class DataDuplicationException extends DataException {

  private static final long serialVersionUID = 1L;

  public DataDuplicationException(String message) {
    super(message);
  }

  public DataDuplicationException(Throwable cause) {
    super(cause);
  }

  public DataDuplicationException(String message, Throwable cause) {
    super(message, cause);
  }
}
