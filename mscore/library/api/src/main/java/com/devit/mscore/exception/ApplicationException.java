package com.devit.mscore.exception;

import org.apache.commons.lang3.StringUtils;

/**
 * Root exception for data issue.
 * 
 * @author dkakunsi
 */
public class ApplicationException extends Exception {

    private static final long serialVersionUID = 1L;

    protected String type;

    public ApplicationException(String message) {
        super(message);
    }

    public ApplicationException(Throwable cause) {
        super(cause);
    }

    public ApplicationException(String message, Throwable cause) {
        super(message, cause);
    }

    public String getType() {
        return this.type;
    }

    public ApplicationException withType(String type) {
        this.type = type;
        return this;
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
