package com.devit.mscore.logging;

import org.junit.Before;
import org.junit.Test;

import com.devit.mscore.Logger;

public class ApplicationLoggerTest {

    private Logger logger;

    @Before
    public void setup() {
        this.logger = new ApplicationLogger(ApplicationLoggerTest.class);
    }
    
    @Test
    public void testInfo() {
        this.logger.info("INFO");
    }
    
    @Test
    public void testDebug() {
        this.logger.debug("DEBUG");
    }
    
    @Test
    public void testTrace() {
        this.logger.trace("TRACE");
    }
    
    @Test
    public void testWarn() {
        this.logger.warn("WARN");
    }
    
    @Test
    public void testError() {
        this.logger.error("ERROR");
    }
    
    @Test
    public void testErrorWithArg() {
        this.logger.error("ERROR: {}", "message");
    }

    @Test
    public void testErrorWithFormat() {
        this.logger.error("ERROR: {}, {}", "message", "message2");
    }
}
