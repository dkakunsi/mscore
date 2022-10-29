package com.devit.mscore.history;

import com.devit.mscore.History;
import com.devit.mscore.HistoryException;
import com.devit.mscore.Listener;
import com.devit.mscore.Subscriber;
import com.devit.mscore.logging.ApplicationLogger;

import org.json.JSONObject;

public class EventListener extends Listener {

  private History history;

  private EventListener(Subscriber subscriber) {
    super(subscriber, ApplicationLogger.getLogger(EventListener.class));
  }

  public static EventListener of(Subscriber subscriber) {
    return new EventListener(subscriber);
  }

  public static EventListener of(Subscriber subscriber, History history) {
    return new EventListener(subscriber).with(history);
  }

  public EventListener with(History history) {
    this.history = history;
    return this;
  }

  @Override
  public void consume(JSONObject message) {
    logger.debug("Receive event message: {}", message);
    try {
      this.history.create(message);
    } catch (HistoryException ex) {
      logger.error("Failed to create history", ex);
    }
  }
}
