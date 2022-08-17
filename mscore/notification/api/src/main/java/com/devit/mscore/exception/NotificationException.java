package com.devit.mscore.exception;

/**
 * Exception in notification process.
 * 
 * @author dkakunsi
 */
public class NotificationException extends ApplicationException {

  private static final long serialVersionUID = 1L;

  public NotificationException(String message) {
    super(message);
  }

  public NotificationException(Throwable cause) {
    super(cause);
  }

  public NotificationException(String message, Throwable cause) {
    super(message, cause);
  }
}
