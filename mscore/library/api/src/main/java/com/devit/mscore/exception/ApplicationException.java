package com.devit.mscore.exception;

import org.apache.commons.lang3.StringUtils;

/**
 * Root exception for data issue.
 * 
 * @author dkakunsi
 */
public class ApplicationException extends Exception {

  private static final long serialVersionUID = 1L;

  protected final String type;

  public ApplicationException(String message) {
    super(message);
    this.type = null;
  }

  public ApplicationException(String message, String type) {
    super(message);
    this.type = type;
  }

  public ApplicationException(Throwable cause) {
    super(cause);
    this.type = null;
  }

  public ApplicationException(Throwable cause, String type) {
    super(cause);
    this.type = type;
  }

  public ApplicationException(String message, Throwable cause) {
    super(message, cause);
    this.type = null;
  }

  public ApplicationException(String message, Throwable cause, String type) {
    super(message, cause);
    this.type = type;
  }

  public String getType() {
    return this.type;
  }

  @Override
  public String getMessage() {
    var message = super.getMessage();
    var cause = getCause();
    var messageIsProper = StringUtils.isNotBlank(message) && !StringUtils.containsIgnoreCase(message, "exception");
    var causeHasProperMessage = cause != null && StringUtils.isNotBlank(cause.getMessage());
    return !messageIsProper && causeHasProperMessage ? cause.getMessage() : message;
  }
}
