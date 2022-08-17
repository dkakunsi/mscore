package com.devit.mscore.data.synchronization;

import static com.devit.mscore.util.AttributeConstants.getDomain;
import static com.devit.mscore.util.AttributeConstants.getId;

import com.devit.mscore.Listener;
import com.devit.mscore.Logger;
import com.devit.mscore.Subscriber;
import com.devit.mscore.logging.ApplicationLogger;

import org.json.JSONObject;

public class SynchronizationListener extends Listener {

  private static final Logger LOG = ApplicationLogger.getLogger(SynchronizationListener.class);

  protected SynchronizationsExecutor synchronizer;

  public SynchronizationListener(Subscriber subscriber, SynchronizationsExecutor synchronizer) {
    super(subscriber);
    this.synchronizer = synchronizer;
  }

  @Override
  protected void consume(JSONObject message) {
    LOG.info("External dependency {} of {} domain is updated. Trying to sync references.", getId(message),
        getDomain(message));
    this.synchronizer.execute(message);
    LOG.debug("Dependency is synced: {}", message);
  }
}
