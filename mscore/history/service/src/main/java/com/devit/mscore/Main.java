package com.devit.mscore;

import com.devit.mscore.exception.ApplicationException;
import com.devit.mscore.history.ApplicationStarter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws ApplicationException {
        var applicationStarter = new ApplicationStarter(args);
        try {
            LOGGER.info("Service is starting...");
            applicationStarter.start();
            LOGGER.info("Service is started!");
        } catch (RuntimeException ex) {
            throw new ApplicationException("Service is fail to start.", ex);
        }
    }
}
