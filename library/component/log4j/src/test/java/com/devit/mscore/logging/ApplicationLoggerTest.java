package com.devit.mscore.logging;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;

public class ApplicationLoggerTest {

  private org.slf4j.Logger logger;

  private Exception exception;

  @Before
  public void setup() {
    this.logger = mock(org.slf4j.Logger.class);
    doReturn(true).when(this.logger).isDebugEnabled();
    doReturn(true).when(this.logger).isInfoEnabled();
    doReturn(true).when(this.logger).isWarnEnabled();
    doReturn(true).when(this.logger).isErrorEnabled();
    doReturn(true).when(this.logger).isTraceEnabled();
    this.exception = new Exception();
  }

  @Test
  public void testInfo() {
    try (MockedStatic<LoggerFactory> utilities = Mockito.mockStatic(LoggerFactory.class)) {
      utilities.when(() -> LoggerFactory.getLogger(any(Class.class))).thenReturn(this.logger);

      var applicationLogger = ApplicationLogger.getLogger(ApplicationLoggerTest.class);
      applicationLogger.info("INFO");
      verify(this.logger).info("BreadcrumbId: 'NOT-SPECIFIED'. INFO");
    }
  }

  @Test
  public void testInfo_WithFormat() {
    try (MockedStatic<LoggerFactory> utilities = Mockito.mockStatic(LoggerFactory.class)) {
      utilities.when(() -> LoggerFactory.getLogger(any(Class.class))).thenReturn(this.logger);

      var applicationLogger = ApplicationLogger.getLogger(ApplicationLoggerTest.class);
      applicationLogger.info("{}", "INFO");
      verify(this.logger).info("BreadcrumbId: 'NOT-SPECIFIED'. INFO");
    }
  }

  @Test
  public void testDebug() {
    try (MockedStatic<LoggerFactory> utilities = Mockito.mockStatic(LoggerFactory.class)) {
      utilities.when(() -> LoggerFactory.getLogger(any(Class.class))).thenReturn(this.logger);

      var applicationLogger = ApplicationLogger.getLogger(ApplicationLoggerTest.class);
      applicationLogger.debug("DEBUG");
      verify(this.logger).debug("BreadcrumbId: 'NOT-SPECIFIED'. DEBUG");
    }
  }

  @Test
  public void testDebug_WithFormat() {
    try (MockedStatic<LoggerFactory> utilities = Mockito.mockStatic(LoggerFactory.class)) {
      utilities.when(() -> LoggerFactory.getLogger(any(Class.class))).thenReturn(this.logger);

      var applicationLogger = ApplicationLogger.getLogger(ApplicationLoggerTest.class);
      applicationLogger.debug("{}", "DEBUG");
      verify(this.logger).debug("BreadcrumbId: 'NOT-SPECIFIED'. DEBUG");
    }
  }

  @Test
  public void testTrace() {
    try (MockedStatic<LoggerFactory> utilities = Mockito.mockStatic(LoggerFactory.class)) {
      utilities.when(() -> LoggerFactory.getLogger(any(Class.class))).thenReturn(this.logger);

      var applicationLogger = ApplicationLogger.getLogger(ApplicationLoggerTest.class);
      applicationLogger.trace("TRACE");
      verify(this.logger).trace("BreadcrumbId: 'NOT-SPECIFIED'. TRACE");
    }
  }

  @Test
  public void testTrace_WithFormat() {
    try (MockedStatic<LoggerFactory> utilities = Mockito.mockStatic(LoggerFactory.class)) {
      utilities.when(() -> LoggerFactory.getLogger(any(Class.class))).thenReturn(this.logger);

      var applicationLogger = ApplicationLogger.getLogger(ApplicationLoggerTest.class);
      applicationLogger.trace("{}", "TRACE");
      verify(this.logger).trace("BreadcrumbId: 'NOT-SPECIFIED'. TRACE");
    }
  }

  @Test
  public void testWarn() {
    try (MockedStatic<LoggerFactory> utilities = Mockito.mockStatic(LoggerFactory.class)) {
      utilities.when(() -> LoggerFactory.getLogger(any(Class.class))).thenReturn(this.logger);

      var applicationLogger = ApplicationLogger.getLogger(ApplicationLoggerTest.class);
      applicationLogger.warn("WARN");
      verify(this.logger).warn("BreadcrumbId: 'NOT-SPECIFIED'. WARN");
    }
  }

  @Test
  public void testWarn_WithFormat() {
    try (MockedStatic<LoggerFactory> utilities = Mockito.mockStatic(LoggerFactory.class)) {
      utilities.when(() -> LoggerFactory.getLogger(any(Class.class))).thenReturn(this.logger);

      var applicationLogger = ApplicationLogger.getLogger(ApplicationLoggerTest.class);
      applicationLogger.warn("{}", "WARN");
      verify(this.logger).warn("BreadcrumbId: 'NOT-SPECIFIED'. WARN");
    }
  }

  @Test
  public void testError() {
    try (MockedStatic<LoggerFactory> utilities = Mockito.mockStatic(LoggerFactory.class)) {
      utilities.when(() -> LoggerFactory.getLogger(any(Class.class))).thenReturn(this.logger);

      var applicationLogger = ApplicationLogger.getLogger(ApplicationLoggerTest.class);
      applicationLogger.error("ERROR");
      verify(this.logger).error("BreadcrumbId: 'NOT-SPECIFIED'. ERROR");
    }
  }

  @Test
  public void testError_WithThrowable() {
    try (MockedStatic<LoggerFactory> utilities = Mockito.mockStatic(LoggerFactory.class)) {
      utilities.when(() -> LoggerFactory.getLogger(any(Class.class))).thenReturn(this.logger);

      var applicationLogger = ApplicationLogger.getLogger(ApplicationLoggerTest.class);
      applicationLogger.error("ERROR", this.exception);
      verify(this.logger).error("BreadcrumbId: 'NOT-SPECIFIED'. ERROR", this.exception);
    }
  }

  @Test
  public void testError_WithSingleArg() {
    try (MockedStatic<LoggerFactory> utilities = Mockito.mockStatic(LoggerFactory.class)) {
      utilities.when(() -> LoggerFactory.getLogger(any(Class.class))).thenReturn(this.logger);

      var applicationLogger = ApplicationLogger.getLogger(ApplicationLoggerTest.class);
      applicationLogger.error("ERROR: {}", "message");
      verify(this.logger).error("BreadcrumbId: 'NOT-SPECIFIED'. ERROR: message");
    }
  }

  @Test
  public void testError_WithFormat() {
    try (MockedStatic<LoggerFactory> utilities = Mockito.mockStatic(LoggerFactory.class)) {
      utilities.when(() -> LoggerFactory.getLogger(any(Class.class))).thenReturn(this.logger);

      var applicationLogger = ApplicationLogger.getLogger(ApplicationLoggerTest.class);
      applicationLogger.error("ERROR: {}. {}", "message", "message2");
      verify(this.logger).error("BreadcrumbId: 'NOT-SPECIFIED'. ERROR: message. message2");
    }
  }

  @Test
  public void testError_WithArgAndThrowable() {
    try (MockedStatic<LoggerFactory> utilities = Mockito.mockStatic(LoggerFactory.class)) {
      utilities.when(() -> LoggerFactory.getLogger(any(Class.class))).thenReturn(this.logger);

      var applicationLogger = ApplicationLogger.getLogger(ApplicationLoggerTest.class);
      applicationLogger.error("ERROR: {}", this.exception, "message");
      verify(this.logger).error("BreadcrumbId: 'NOT-SPECIFIED'. ERROR: message", this.exception);
    }
  }

  @Test
  public void testError_WithFormatAndThrowable() {
    try (MockedStatic<LoggerFactory> utilities = Mockito.mockStatic(LoggerFactory.class)) {
      utilities.when(() -> LoggerFactory.getLogger(any(Class.class))).thenReturn(this.logger);

      var applicationLogger = ApplicationLogger.getLogger(ApplicationLoggerTest.class);
      applicationLogger.error("ERROR: {}. {}", this.exception, "message", "message2");
      verify(this.logger).error("BreadcrumbId: 'NOT-SPECIFIED'. ERROR: message. message2", this.exception);
    }
  }
}
