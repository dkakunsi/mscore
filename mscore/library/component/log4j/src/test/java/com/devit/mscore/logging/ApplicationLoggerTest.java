package com.devit.mscore.logging;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;

import com.devit.mscore.Logger;

public class ApplicationLoggerTest {

    private Logger logger;

    @Before
    public void setup() {
        this.logger = spy(ApplicationLogger.getLogger(ApplicationLoggerTest.class));
    }
    
    @Test
    public void testInfo() {
        this.logger.info("INFO");
        verify(this.logger, times(1)).info("INFO");
    }
    
    @Test
    public void testDebug() {
        this.logger.debug("DEBUG");
        verify(this.logger, times(1)).debug("DEBUG");
    }
    
    @Test
    public void testTrace() {
        this.logger.trace("TRACE");
        verify(this.logger, times(1)).trace("TRACE");
    }
    
    @Test
    public void testWarn() {
        this.logger.warn("WARN");
        verify(this.logger, times(1)).warn("WARN");
    }
    
    @Test
    public void testError() {
        this.logger.error("ERROR");
        verify(this.logger, times(1)).error("ERROR");
    }
    
    @Test
    public void testErrorWithArg() {
        this.logger.error("ERROR: {}", "message");
        verify(this.logger, times(1)).error("ERROR: {}", "message");
    }

    @Test
    public void testErrorWithFormat() {
        this.logger.error("ERROR: {}. {}", "message", "message2");
        verify(this.logger, times(1)).error("ERROR: {}. {}", "message", "message2");
    }
}
