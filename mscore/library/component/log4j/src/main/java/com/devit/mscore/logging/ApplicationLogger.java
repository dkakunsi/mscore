package com.devit.mscore.logging;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

import static com.devit.mscore.ApplicationContext.getContext;

import com.devit.mscore.Logger;

public class ApplicationLogger implements Logger {

    private static final String MESSAGE_FORMAT = "BreadcrumbId: {}. {}";

    private final org.slf4j.Logger logger;

    public ApplicationLogger(Class<?> clazz) {
        this.logger = LoggerFactory.getLogger(clazz);
    }

    public static Logger getLogger(Class<?> clazz) {
        return new ApplicationLogger(clazz);
    }

    private String getBreadcrumbId() {
        try {
            var breadcrumbId = getContext().getBreadcrumbId();
            return StringUtils.isBlank(breadcrumbId) ? "NOT-SPECIFIED" : breadcrumbId;
        } catch (Exception ex) {
            return "NOT-SPECIFIED";
        }
    }

    @Override
    public void debug(String message) {
        debug(MESSAGE_FORMAT, getBreadcrumbId(), message);
    }

    @Override
    public void debug(String format, Object...args) {
        var messageFormat = "BreadcrumbId: {}. " + format;
        this.logger.debug(messageFormat, getBreadcrumbId(), args);
    }

    @Override
    public void error(String message) {
        error(message, (Throwable) null);
    }

    @Override
    public void error(String message, String arg) {
        error(message, null, arg);
    }

    @Override
    public void error(String message, Throwable ex) {
        error(MESSAGE_FORMAT, ex, getBreadcrumbId(), message);
    }

    @Override
    public void error(String format, Object...args) {
        error(format, null, args);
    }

    @Override
    public void error(String format, Throwable ex, Object...args) {
        var messageFormat = "BreadcrumbId: {}. " + format;
        this.logger.error(messageFormat, getBreadcrumbId(), args, ex);
    }

    @Override
    public void info(String message) {
        info(MESSAGE_FORMAT, getBreadcrumbId(), message);
    }

    @Override
    public void info(String format, Object...args) {
        var messageFormat = "BreadcrumbId: {}. " + format;
        this.logger.info(messageFormat, getBreadcrumbId(), args);
    }

    @Override
    public void trace(String message) {
        trace(MESSAGE_FORMAT, getBreadcrumbId(), message);
    }

    @Override
    public void trace(String format, Object...args) {
        var messageFormat = "BreadcrumbId: {}. " + format;
        this.logger.trace(messageFormat, getBreadcrumbId(), args);
    }

    @Override
    public void warn(String message) {
        warn(MESSAGE_FORMAT, getBreadcrumbId(), message);
    }

    @Override
    public void warn(String format, Object...args) {
        var messageFormat = "BreadcrumbId: {}. " + format;
        this.logger.warn(messageFormat, getBreadcrumbId(), args);
    }
}
