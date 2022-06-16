package com.devit.mscore.exception;

public class MessagingException extends ApplicationException {

    private static final long serialVersionUID = 1L;

    public MessagingException(String message) {
      super(message);
    }

    public MessagingException(Throwable cause) {
      super(cause);
    }

    public MessagingException(String message, Throwable cause) {
      super(message, cause);
    }
}
