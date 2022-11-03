package com.devit.mscore.data.observer;

import static com.devit.mscore.util.AttributeConstants.getDomain;
import static com.devit.mscore.util.AttributeConstants.getId;

import com.devit.mscore.Logger;
import com.devit.mscore.PostProcessObserver;
import com.devit.mscore.Synchronization;
import com.devit.mscore.exception.SynchronizationException;
import com.devit.mscore.logging.ApplicationLogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

public class SynchronizationObserver implements PostProcessObserver {

  private static final Logger LOG = ApplicationLogger.getLogger(SynchronizationObserver.class);

  private Map<String, List<Synchronization>> synchronizations;

  public SynchronizationObserver() {
    synchronizations = new HashMap<>();
  }

  public void add(Synchronization synchronization) {
    var domain = synchronization.getReferenceDomain();
    this.synchronizations.computeIfAbsent(domain, key -> new ArrayList<>());
    this.synchronizations.get(domain).add(synchronization);
  }

  @Override
  public void notify(JSONObject message) {
    LOG.info("Synchronizing all object that depends to '{}' of domain '{}'", getId(message), getDomain(message));

    var referenceDomain = getDomain(message);
    var referenceId = getId(message);
    if (StringUtils.isEmpty(referenceDomain)) {
      LOG.warn("Fail to synchronize object '{}' synce the domain is not provided", referenceId);
      return;
    }
    synchronize(this.synchronizations.get(referenceDomain), referenceId);

    LOG.info("Dependencies of object '{}' of domain '{}' are all synced", getId(message), getDomain(message));
  }

  private void synchronize(List<Synchronization> synchronizations, String referenceId) {
    if (synchronizations == null || StringUtils.isBlank(referenceId)) {
      return;
    }

    synchronizations.forEach(s -> {
      try {
        s.synchronize(referenceId);
      } catch (SynchronizationException ex) {
        LOG.error("Cannot synchronize object '{}'", ex, referenceId);
      }
    });
  }
}
