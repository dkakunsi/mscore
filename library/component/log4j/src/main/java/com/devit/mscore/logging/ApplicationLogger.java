package com.devit.mscore.logging;

import static com.devit.mscore.ApplicationContext.getContext;

import com.devit.mscore.Logger;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;

public class ApplicationLogger implements Logger {

  private final org.slf4j.Logger logger;

  private ApplicationLogger(Class<?> clazz) {
    this.logger = LoggerFactory.getLogger(clazz);
  }

  public static Logger getLogger(Class<?> clazz) {
    return new ApplicationLogger(clazz);
  }

  public static Logger of(Class<?> clazz) {
    return new ApplicationLogger(clazz);
  }

  @Override
  public void debug(String message) {
    this.logger.debug(getMessage(message));
  }

  @Override
  public void debug(String format, Object... args) {
    this.logger.debug(getMessage(format, args));
  }

  @Override
  public void error(String message) {
    this.logger.error(getMessage(message));
  }

  @Override
  public void error(String message, Throwable ex) {
    this.logger.error(getMessage(message), ex);
  }

  @Override
  public void error(String format, Throwable ex, Object... args) {
    this.logger.error(getMessage(format, args), ex);
  }

  @Override
  public void error(String format, String arg) {
    this.logger.error(getMessage(format, arg));
  }

  @Override
  public void error(String format, Object... args) {
    this.logger.error(getMessage(format, args));
  }

  @Override
  public void info(String message) {
    this.logger.info(getMessage(message));
  }

  @Override
  public void info(String format, Object... args) {
    this.logger.info(getMessage(format, args));
  }

  @Override
  public void trace(String message) {
    this.logger.trace(getMessage(message));
  }

  @Override
  public void trace(String format, Object... args) {
    this.logger.trace(getMessage(format, args));
  }

  @Override
  public void warn(String message) {
    this.logger.warn(getMessage(message));
  }

  @Override
  public void warn(String format, Object... args) {
    this.logger.warn(getMessage(format, args));
  }

  private String getMessage(String format, Object... args) {
    var formatter = MessageFormatter.arrayFormat(format, args);
    return getMessage(formatter.getMessage());
  }

  private String getMessage(String message) {
    var formatter = MessageFormatter.format("BreadcrumbId: {}. {}", getBreadcrumbId(), message);
    return formatter.getMessage();
  }

  private String getBreadcrumbId() {
    try {
      var breadcrumbId = getContext().getBreadcrumbId();
      return StringUtils.isBlank(breadcrumbId) ? "NOT-SPECIFIED" : breadcrumbId;
    } catch (Exception ex) {
      return "NOT-SPECIFIED";
    }
  }
}
