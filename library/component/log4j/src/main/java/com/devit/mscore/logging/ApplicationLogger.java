package com.devit.mscore.logging;

import static com.devit.mscore.ApplicationContext.getContext;

import com.devit.mscore.Logger;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

public class ApplicationLogger implements Logger {

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
    debug("{}", message);
  }

  @Override
  public void debug(String format, Object... args) {
    var messageFormat = "BreadcrumbId: {}. " + format;
    this.logger.debug(messageFormat, getBreadcrumbId(), args);
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
    error("{}", message);
  }

  @Override
  public void error(String format, Object... args) {
    error(format, null, args);
  }

  @Override
  public void error(String format, Throwable ex, Object... args) {
    this.logger.error(getMessageFormat(format), getBreadcrumbId(), args, ex);
  }

  @Override
  public void info(String message) {
    info("{}", message);
  }

  @Override
  public void info(String format, Object... args) {
    this.logger.info(getMessageFormat(format), getBreadcrumbId(), args);
  }

  @Override
  public void trace(String message) {
    trace("{}", message);
  }

  @Override
  public void trace(String format, Object... args) {
    this.logger.trace(getMessageFormat(format), getBreadcrumbId(), args);
  }

  @Override
  public void warn(String message) {
    warn("{}", message);
  }

  @Override
  public void warn(String format, Object... args) {
    this.logger.warn(getMessageFormat(format), getBreadcrumbId(), args);
  }

  private static String getMessageFormat(String originalFormat) {
    return "BreadcrumbId: {}. " + originalFormat;
  }
}
