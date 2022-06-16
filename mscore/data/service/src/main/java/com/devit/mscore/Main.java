package com.devit.mscore;
import com.devit.mscore.data.ApplicationStarter;
import com.devit.mscore.exception.ApplicationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws ApplicationException {
        var starter = ApplicationStarter.of(args);
        try {
            LOGGER.info("Starting services!");
            starter.start();
            LOGGER.info("Service is started!");
        } catch (ApplicationException ex) {
            LOGGER.error("Cannot start the application.", ex);
            starter.stop();
        }
    }
}
