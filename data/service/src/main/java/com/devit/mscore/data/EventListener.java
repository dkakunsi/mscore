package com.devit.mscore.data;

import static com.devit.mscore.util.AttributeConstants.getId;

import com.devit.mscore.Event;
import com.devit.mscore.Listener;
import com.devit.mscore.Logger;
import com.devit.mscore.Service;
import com.devit.mscore.Subscriber;
import com.devit.mscore.exception.ApplicationException;
import com.devit.mscore.exception.ApplicationRuntimeException;
import com.devit.mscore.logging.ApplicationLogger;

import java.util.Map;

import org.json.JSONObject;

public class EventListener extends Listener {

  private static final Logger LOGGER = ApplicationLogger.getLogger(EventListener.class);

  private Map<String, Service> services;

  private EventListener(Subscriber subscriber, Map<String, Service> services) {
    super(subscriber);
    this.services = services;
  }

  public static EventListener of(Subscriber subscriber, Map<String, Service> services) {
    if (services.isEmpty()) {
      throw new ApplicationRuntimeException("Empty services is passed to EventListener");
    }
    return new EventListener(subscriber, services);
  }

  @SuppressWarnings("PMD.GuardLogStatement")
  @Override
  protected void consume(JSONObject message) {
    var event = Event.of(message);
    var service = this.services.get(message.getString(Event.DOMAIN));
    try {
      if (Event.Type.CREATE.equals(event.getType()) || Event.Type.UPDATE.equals(event.getType())) {
        service.save(event.getData());
      } else if (Event.Type.REMOVE.equals(event.getType())) {
        service.delete(getId(event.getData()));
      } else {
        LOGGER.info("Cannot process event type: {}", event.getType());
      }
    } catch (ApplicationException ex) {
      LOGGER.error("Fail processing the event.", ex);
    }
  }
}
