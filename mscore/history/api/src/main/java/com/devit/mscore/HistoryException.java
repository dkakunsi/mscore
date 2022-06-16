package com.devit.mscore;

import com.devit.mscore.exception.ApplicationException;

public class HistoryException extends ApplicationException {
    
    private static final long serialVersionUID = 1L;

    public HistoryException(String message) {
        super(message);
    }

    public HistoryException(Throwable cause) {
        super(cause);
    }

    public HistoryException(String message, Throwable cause) {
        super(message, cause);
    }

}
