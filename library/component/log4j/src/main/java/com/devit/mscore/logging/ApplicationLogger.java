package com.devit.mscore.logging;

import static com.devit.mscore.ApplicationContext.getContext;

import com.devit.mscore.Logger;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

public class ApplicationLogger implements Logger {

  private final org.slf4j.Logger logger;

  private ApplicationLogger(Class<?> clazz) {
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
    debug("{}", message);
  }

  @Override
  public void debug(String format, Object... args) {
    this.logger.debug(getMessage(format, args));
  }

  @Override
  public void error(String message) {
    error(message, (Throwable) null);
  }

  @Override
  public void error(String format, String arg) {
    error(format, null, arg);
  }

  @Override
  public void error(String message, Throwable ex) {
    error("{}", ex, message);
  }

  @Override
  public void error(String format, Object... args) {
    error(format, null, args);
  }

  @Override
  public void error(String format, Throwable ex, Object... args) {
    this.logger.error(getMessage(format, args), ex);
  }

  @Override
  public void info(String message) {
    info("{}", message);
  }

  @Override
  public void info(String format, Object... args) {
    this.logger.info(getMessage(format, args));
  }

  @Override
  public void trace(String message) {
    trace("{}", message);
  }

  @Override
  public void trace(String format, Object... args) {
    this.logger.trace(getMessage(format, args));
  }

  @Override
  public void warn(String message) {
    warn("{}", message);
  }

  @Override
  public void warn(String format, Object... args) {
    this.logger.warn(getMessage(format, args));
  }

  private String getMessage(String message, Object... args) {
    Object[] breadcrumbId = { getBreadcrumbId() };
    FormattingTuple formattingTuple;
    if (args != null) {
      formattingTuple = MessageFormatter.arrayFormat("BreadcrumbId: {}. " + message, ArrayUtils.addAll(breadcrumbId, args));
    } else {
      formattingTuple = MessageFormatter.arrayFormat("BreadcrumbId: {}. " + message, breadcrumbId);
    }
    return formattingTuple.getMessage();
  }
}
