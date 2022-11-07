package com.devit.mscore.workflow;

import static com.devit.mscore.util.AttributeConstants.getId;

import com.devit.mscore.Event;
import com.devit.mscore.Listener;
import com.devit.mscore.Subscriber;
import com.devit.mscore.WorkflowService;
import com.devit.mscore.exception.ProcessException;
import com.devit.mscore.exception.RegistryException;
import com.devit.mscore.logging.ApplicationLogger;

import java.util.HashMap;

import org.json.JSONObject;

public class EventListener extends Listener {

  private WorkflowService service;

  private EventListener(Subscriber subscriber, WorkflowService service) {
    super(subscriber, ApplicationLogger.getLogger(EventListener.class));
    this.service = service;
  }

  public static EventListener of(Subscriber subscriber, WorkflowService service) {
    return new EventListener(subscriber, service);
  }

  @Override
  protected void consume(JSONObject message) {
    var event = Event.of(message);
    logger.info("Processing event '{}' for domain '{}'", event.getType(), event.getDomain());
    try {
      processEvent(event);
    } catch (ProcessException ex) {
      if (ex.getCause() instanceof RegistryException) {
        logger.warn("Cannot process event");
      } else {
        logger.error("Fail processing event", ex);
      }
    }
  }

  private void processEvent(Event event) throws ProcessException {
    if (event.isDomainEvent()) {
      var instance = this.service.executeWorkflow(event.getAction(), event.getData(), new HashMap<>());
      logger.info("Instance is created with id '{}'", instance.getId());
    } else {
      var data = event.getData();
      var taskId = getId(data);
      this.service.completeTask(taskId, data.getJSONObject("response").toMap());
      logger.info("Task '{}' is completed", taskId);
    }
  }
}
