package com.devit.mscore.data;

import static com.devit.mscore.util.AttributeConstants.getCode;
import static com.devit.mscore.util.AttributeConstants.getId;

import com.devit.mscore.Event;
import com.devit.mscore.Listener;
import com.devit.mscore.Service;
import com.devit.mscore.Subscriber;
import com.devit.mscore.exception.ApplicationException;
import com.devit.mscore.exception.ApplicationRuntimeException;
import com.devit.mscore.logging.ApplicationLogger;

import java.util.Map;

import org.json.JSONObject;

public class EventListener extends Listener {

  private Map<String, Service> services;

  private EventListener(Subscriber subscriber, Map<String, Service> services) {
    super(subscriber, ApplicationLogger.getLogger(EventListener.class));
    this.services = services;
  }

  public static EventListener of(Subscriber subscriber, Map<String, Service> services) {
    if (services.isEmpty()) {
      throw new ApplicationRuntimeException("Empty services is passed to EventListener");
    }
    return new EventListener(subscriber, services);
  }

  @Override
  protected void consume(JSONObject message) {
    var event = Event.of(message);
    var code = getCode(event.getData());
    logger.info("Processing '{}'' for code '{}'", event.getAction(), code);
    var service = this.services.get(message.getString(Event.DOMAIN));
    try {
      if (Event.Type.CREATE.equals(event.getType()) || Event.Type.UPDATE.equals(event.getType())) {
        service.save(event.getData());
      } else if (Event.Type.REMOVE.equals(event.getType())) {
        service.delete(getId(event.getData()));
      } else {
        logger.info("Cannot process '{}' for code '{}'", event.getType(), code);
      }
    } catch (ApplicationException ex) {
      logger.error("Fail processing event '{}' for '{}'", ex, event.getAction(), code);
    }
  }
}
