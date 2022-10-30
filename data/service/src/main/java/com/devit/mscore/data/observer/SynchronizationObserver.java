package com.devit.mscore.data.observer;

import static com.devit.mscore.util.AttributeConstants.getCode;
import static com.devit.mscore.util.AttributeConstants.getDomain;

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
    this.executor.execute(message);

    var domain = getDomain(message);
    var code = getCode(message);
    LOG.info("Dependencies are synced for domain {} and code {}", domain, code);
  }
}
