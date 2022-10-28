package com.devit.mscore;

import static com.devit.mscore.ApplicationContext.setContext;

import com.devit.mscore.exception.ApplicationException;
import com.devit.mscore.history.ApplicationStarter;
import com.devit.mscore.logging.ApplicationLogger;

public class Main {

  private static final Logger LOGGER = ApplicationLogger.getLogger(Main.class);

  public static void main(String[] args) throws ApplicationException {
    setContext(DefaultApplicationContext.of("starter"));
    try {
      var applicationStarter = new ApplicationStarter(args);
      LOGGER.info("Service is starting...");
      applicationStarter.start();
      LOGGER.info("Service is started!");
    } catch (RuntimeException ex) {
      throw new ApplicationException("Service is fail to start", ex);
    }
  }
}
