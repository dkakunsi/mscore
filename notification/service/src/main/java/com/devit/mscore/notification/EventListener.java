package com.devit.mscore.notification;

import com.devit.mscore.Listener;
import com.devit.mscore.Notification;
import com.devit.mscore.Subscriber;
import com.devit.mscore.exception.NotificationException;
import com.devit.mscore.logging.ApplicationLogger;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

public class EventListener extends Listener {

  private List<Notification> notifications;

  private EventListener(Subscriber subscriber) {
    super(subscriber, ApplicationLogger.getLogger(EventListener.class));
    this.notifications = new ArrayList<>();
  }

  public static EventListener of(Subscriber subscriber) {
    return new EventListener(subscriber);
  }

  public static EventListener of(Subscriber subscriber, Notification notification) {
    return new EventListener(subscriber).with(notification);
  }

  public EventListener with(Notification notification) {
    this.notifications.add(notification);
    return this;
  }

  @Override
  public void consume(JSONObject message) {
    logger.debug("Receive event message: {}", message);
    this.notifications.forEach(notification -> {
      try {
        logger.info("Sending '{}' notification", notification.getType());
        notification.send(message);
      } catch (NotificationException ex) {
        logger.error("Notification failed", ex);
      }
    });
  }
}
