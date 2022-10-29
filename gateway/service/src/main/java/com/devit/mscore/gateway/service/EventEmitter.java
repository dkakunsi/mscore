package com.devit.mscore.gateway.service;

import static com.devit.mscore.util.AttributeConstants.ID;
import static com.devit.mscore.util.AttributeConstants.getId;
import static com.devit.mscore.util.AttributeConstants.hasId;

import com.devit.mscore.Event;
import com.devit.mscore.Publisher;
import com.devit.mscore.Service;
import com.devit.mscore.util.AttributeConstants;

import java.util.UUID;

import org.json.JSONObject;

public class EventEmitter implements Service {

  private Publisher publisher;

  private String domainEventChannel;

  private String workflowEventChannel;

  public EventEmitter(Publisher publisher, String domainEventChannel, String workflowEventChannel) {
    this.publisher = publisher;
    this.domainEventChannel = domainEventChannel;
    this.workflowEventChannel = workflowEventChannel;
  }

  @Override
  public String getDomain() {
    return "event";
  }

  public String create(JSONObject data) {
    return emitEvent(data, Event.Type.CREATE, domainEventChannel);
  }

  public String update(JSONObject data) {
    return emitEvent(data, Event.Type.UPDATE, domainEventChannel);
  }

  public String remove(JSONObject data) {
    return emitEvent(data, Event.Type.REMOVE, domainEventChannel);
  }

  public String updateTask(String taskId, JSONObject taskResponse) {
    var data = new JSONObject();
    data.put(ID, taskId);
    data.put("response", taskResponse);
    return emitEvent(data, Event.Type.TASK, workflowEventChannel);
  }

  public String emitEvent(JSONObject data, Event.Type eventType, String channel) {
    var id = getOrCreateId(data);
    var domain = AttributeConstants.getDomain(data);
    var event = Event.of(eventType, domain, data);
    this.publisher.publish(channel, event.toJson());
    return id;
  }

  private static String getOrCreateId(JSONObject json) {
    if (!hasId(json)) {
      var id = UUID.randomUUID().toString();
      json.put(ID, id);
    }
    return getId(json);
  }
}
