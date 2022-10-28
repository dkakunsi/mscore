package com.devit.mscore;

import static com.devit.mscore.ApplicationContext.setContext;

import com.devit.mscore.data.ApplicationStarter;
import com.devit.mscore.exception.ApplicationException;
import com.devit.mscore.logging.ApplicationLogger;

public class Main {

  private static final Logger LOGGER = ApplicationLogger.getLogger(Main.class);

  public static void main(String[] args) throws ApplicationException {
    setContext(DefaultApplicationContext.of("starter"));
    var starter = ApplicationStarter.of(args);
    try {
      LOGGER.info("Starting services!");
      starter.start();
      LOGGER.info("Service is started!");
    } catch (ApplicationException ex) {
      LOGGER.error("Cannot start the application", ex);
      starter.stop();
    }
  }
}
