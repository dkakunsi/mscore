package com.devit.mscore.data.observer;

import static com.devit.mscore.util.AttributeConstants.getDomain;
import static com.devit.mscore.util.AttributeConstants.getId;

import com.devit.mscore.ApplicationContext;
import com.devit.mscore.Event;
import com.devit.mscore.Logger;
import com.devit.mscore.Publisher;
import com.devit.mscore.logging.ApplicationLogger;

import org.json.JSONObject;

public class PublishingObserver implements PostProcessObserver {

  private static final Logger LOG = ApplicationLogger.getLogger(PublishingObserver.class);

  private static final String PUBLISHING_ERROR = "Publishing message failed";

  protected Publisher publisher;

  protected Long delay;

  public PublishingObserver(Publisher publisher, Long delay) {
    this.publisher = publisher;
    this.delay = delay;
  }

  @Override
  public void notify(JSONObject json) {
    if (this.publisher == null) {
      LOG.warn("Publisher is not provided. By pass publishing");
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

    var event = createEvent(json);
    LOG.info("Publishing message to topic {} for key {}", this.publisher.getChannel(), getId(json));
    this.publisher.publish(event.toJson());
  }

  private Event createEvent(JSONObject json) {
    var context = ApplicationContext.getContext();
    var eventType = Event.Type.valueOf(context.getAction().get().toUpperCase());
    return Event.of(eventType, getDomain(json), json);
  }
}
