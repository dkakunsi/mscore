package com.devit.mscore.data.observer;

import com.devit.mscore.Logger;
import com.devit.mscore.Publisher;
import com.devit.mscore.logging.ApplicationLogger;

import org.json.JSONObject;

public class PublishingObserver implements PostProcessObserver {

  private static final Logger LOG = new ApplicationLogger(PublishingObserver.class);

  private static final String PUBLISHING_ERROR = "Publishing message failed.";

  protected Publisher publisher;

  protected Long delay;

  public PublishingObserver(Publisher publisher, Long delay) {
    this.publisher = publisher;
    this.delay = delay;
  }

  @Override
  @SuppressWarnings("PMD.GuardLogStatement")
  public void notify(JSONObject json) {
    if (this.publisher == null) {
      LOG.warn("Publisher is not provided. By pass publishing.");
      return;
    }

    if (this.delay != null) {
      try {
        // Without delay the synchronization chain will not get the correct data.
        Thread.sleep(this.delay);
      } catch (InterruptedException ex) {
        LOG.error(PUBLISHING_ERROR, ex);
        Thread.currentThread().interrupt();
      }
    }

    LOG.debug("Publishing message to topic {}: {}.", this.publisher.getChannel(), json);
    this.publisher.publish(json);
  }
}