package com.devit.mscore.workflow;

import com.devit.mscore.Event;
import com.devit.mscore.Listener;
import com.devit.mscore.Logger;
import com.devit.mscore.Subscriber;
import com.devit.mscore.WorkflowService;
import com.devit.mscore.exception.ProcessException;
import com.devit.mscore.exception.RegistryException;
import com.devit.mscore.logging.ApplicationLogger;

import java.util.HashMap;

import org.json.JSONObject;

public class EventListener extends Listener {

  private static final Logger LOGGER = ApplicationLogger.getLogger(EventListener.class);

  private WorkflowService service;

  private EventListener(Subscriber subscriber, WorkflowService service) {
    super(subscriber);
    this.service = service;
  }

  public static EventListener of(Subscriber subscriber, WorkflowService service) {
    return new EventListener(subscriber, service);
  }

  @Override
  protected void consume(JSONObject message) {
    var event = Event.of(message);
    LOGGER.info("Processing event {} for domain {}", event.getAction(), event.getDomain());
    try {
      if (event.isDomainEvent()) {
        var instance = this.service.createInstanceByAction(event.getAction(), event.getData(), new HashMap<>());
        LOGGER.info("Instance is created with id {}", instance.getId());
      } else {
        var data = event.getData();
        var taskId = data.getString("id");
        this.service.completeTask(taskId, data.getJSONObject("response"));
        LOGGER.info("Task '{}' is completed", taskId);
      }
    } catch (ProcessException ex) {
      if (ex.getCause() instanceof RegistryException) {
        LOGGER.warn("Cannot process event. " + ex.getMessage());
      } else {
        LOGGER.error("Fail processing event", ex);
      }
    }
  }
}
