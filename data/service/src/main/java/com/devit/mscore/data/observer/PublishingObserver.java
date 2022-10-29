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

    LOG.info("Publishing message to topic {} for key {}", this.publisher.getChannel(), getId(json));
    var event = createEvent(json);
    this.publisher.publish(event.toJson());
  }

  private Event createEvent(JSONObject json) {
    var context = ApplicationContext.getContext();
    var eventType = Event.Type.valueOf(context.getEventType().get().toUpperCase());
    return Event.of(eventType, getDomain(json), json);
  }
}
