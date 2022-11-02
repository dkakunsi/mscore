package com.devit.mscore.data.observer;

import static com.devit.mscore.util.AttributeConstants.getDomain;
import static com.devit.mscore.util.AttributeConstants.getId;

import com.devit.mscore.Logger;
import com.devit.mscore.data.synchronization.SynchronizationsExecutor;
import com.devit.mscore.logging.ApplicationLogger;

import org.json.JSONObject;

public class SynchronizationObserver implements PostProcessObserver {

  private static final Logger LOG = ApplicationLogger.getLogger(SynchronizationObserver.class);

  private SynchronizationsExecutor executor;

  public void setExecutor(SynchronizationsExecutor executor) {
    this.executor = executor;
  }

  @Override
  public void notify(JSONObject message) {
    LOG.info("Synchronizing all object that depends to '{}' of domain '{}'", getId(message), getDomain(message));
    this.executor.execute(message);
    LOG.info("Dependencies of object '{}' of domain '{}' are all synced", getId(message), getDomain(message));
  }
}
