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

  public EventEmitter(Publisher publisher) {
    this.publisher = publisher;
  }

  @Override
  public String getDomain() {
    return "event";
  }

  public String create(JSONObject data) {
    return emitEvent(data, Event.Type.CREATE);
  }

  public String update(JSONObject data) {
    return emitEvent(data, Event.Type.UPDATE);
  }

  public String remove(JSONObject data) {
    return emitEvent(data, Event.Type.REMOVE);
  }

  public String complete(String taskId, JSONObject taskResponse) {
    var data = new JSONObject();
    data.put(ID, taskId);
    data.put("response", taskResponse);
    return emitEvent(data, Event.Type.COMPLETE);
  }

  public String emitEvent(JSONObject data, Event.Type eventType) {
    var id = getOrCreateId(data);
    var domain = AttributeConstants.getDomain(data);
    var event = Event.of(eventType, domain, data);
    this.publisher.publish(event.toJson());
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
