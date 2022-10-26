package com.devit.mscore.data.observer;

import com.devit.mscore.Logger;
import com.devit.mscore.data.synchronization.SynchronizationsExecutor;
import com.devit.mscore.exception.ApplicationRuntimeException;
import com.devit.mscore.logging.ApplicationLogger;

import org.json.JSONObject;

public class SynchronizationObserver implements PostProcessObserver {

  private static final Logger LOG = ApplicationLogger.getLogger(SynchronizationObserver.class);

  private SynchronizationsExecutor executor;

  public void setExecutor(SynchronizationsExecutor executor) {
    try {
      this.executor = (SynchronizationsExecutor) executor.clone();
    } catch (CloneNotSupportedException ex) {
      throw new ApplicationRuntimeException(ex);
    }
  }

  @Override
  public void notify(JSONObject message) {
    this.executor.execute(message);
    LOG.debug("Dependency is synced: {}", message);
  }
}
