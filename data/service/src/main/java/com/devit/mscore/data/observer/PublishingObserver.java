package com.devit.mscore.data.observer;

import static com.devit.mscore.util.AttributeConstants.getDomain;
import static com.devit.mscore.util.AttributeConstants.getId;

import com.devit.mscore.ApplicationContext;
import com.devit.mscore.Event;
import com.devit.mscore.Logger;
import com.devit.mscore.Publisher;
import com.devit.mscore.logging.ApplicationLogger;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

public class PublishingObserver implements PostProcessObserver {

  private static final Logger LOG = ApplicationLogger.getLogger(PublishingObserver.class);

  protected Publisher publisher;

  protected String publishingChannel;

  public PublishingObserver(Publisher publisher, String publishingChannel) {
    this.publisher = publisher;
    this.publishingChannel = publishingChannel;
  }

  @Override
  public void notify(JSONObject json) {
    if (publisher == null || StringUtils.isBlank(publishingChannel)) {
      LOG.warn("Cannot publish event. Channel and/or publisher is not provided");
      return;
    }

    LOG.info("Publishing message of object '{}' to channel '{}'", getId(json), publishingChannel);
    var event = createEvent(json);
    publisher.publish(publishingChannel, event.toJson());
  }

  private Event createEvent(JSONObject json) {
    var context = ApplicationContext.getContext();
    var eventType = Event.Type.valueOf(context.getEventType().get().toUpperCase());
    return Event.of(eventType, getDomain(json), json);
  }
}
