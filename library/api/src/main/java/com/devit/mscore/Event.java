package com.devit.mscore;

import com.devit.mscore.exception.ApplicationRuntimeException;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

public class Event {

  public static final String EVENT = "event";

  public static final String DOMAIN = "domain";

  public static final String DATA = "data";

  public static final String ACTION = "action";

  public static enum Type {
    CREATE("create"),
    UPDATE("update"),
    REMOVE("remove"),
    TASK("task"),
    COMPLETE("complete");

    private String name;

    private Type(String name) {
      this.name = name;
    }

    @Override
    public String toString() {
      return this.name;
    }
  }

  private Type type;

  private String domain;

  private String action;

  private JSONObject data;

  protected Event(Type type, String domain, String action, JSONObject data) {
    this.type = type;
    this.domain = domain;
    this.data = data;
    this.action = action;
  }

  public static Event of(Type type, String domain, String action, JSONObject data) {
    if (StringUtils.isBlank(action)) {
      action = generateAction(domain, type);
    }
    return new Event(type, domain, action, data);
  }

  private static String generateAction(String domain, Type type) {
    return String.format("%s.%s", domain, type);
  }

  public static Event of(JSONObject event) {
    try {
      var type = event.getString(EVENT);
      var domain = event.getString(DOMAIN);
      var data = event.getJSONObject(DATA);
      var action = event.optString(ACTION);
      return of(Type.valueOf(type.toUpperCase()), domain, action, data);
    } catch (Exception ex) {
      throw new ApplicationRuntimeException(ex);
    }
  }

  public String getDomain() {
    return domain;
  }

  public Type getType() {
    return type;
  }

  public String getAction() {
    return action;
  }

  public JSONObject getData() {
    return new JSONObject(data.toMap());
  }

  public boolean isDomainEvent() {
    return Type.CREATE.equals(type)
    || Type.UPDATE.equals(type)
    || Type.REMOVE.equals(type);
  }

  public boolean isWorkflowEvent() {
    return Type.TASK.equals(type);
  }

  public JSONObject toJson() {
    var event = new JSONObject();
    event.put(EVENT, type.toString());
    event.put(DOMAIN, domain);
    event.put(DATA, data);
    event.put(ACTION, action);
    return event;
  }
}
