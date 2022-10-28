package com.devit.mscore;

import static com.devit.mscore.ApplicationContext.setContext;

import com.devit.mscore.exception.ApplicationException;
import com.devit.mscore.logging.ApplicationLogger;
import com.devit.mscore.notification.ApplicationStarter;

public class Main {

  private static final Logger LOGGER = ApplicationLogger.getLogger(Main.class);

  public static void main(String[] args) throws ApplicationException {
    setContext(DefaultApplicationContext.of("starter"));
    var starter = new ApplicationStarter(args);
    try {
      LOGGER.info("Service is starting...");
      starter.start();
      LOGGER.info("Service is started!");
    } catch (RuntimeException ex) {
      throw new ApplicationException("Service is fail to start", ex);
    }
  }
}
