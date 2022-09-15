package com.devit.mscore;

import com.devit.mscore.exception.ApplicationRuntimeException;

import org.json.JSONObject;

public final class Event {

  public static final String EVENT = "event";

  public static final String DOMAIN = "domain";

  public static final String DATA = "data";

  public static enum Type {
    CREATE("create"),
    UPDATE("update"),
    REMOVE("remove"),
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

  private JSONObject data;

  private Event(Type type, String domain, JSONObject data) {
    this.type = type;
    this.domain = domain;
    this.data = data;
  }

  public static Event of(Type type, String domain, JSONObject data) {
    return new Event(type, domain, data);
  }

  public static Event of(JSONObject event) {
    try {
      var type = event.getString(EVENT);
      var domain = event.getString(DOMAIN);
      var data = event.getJSONObject(DATA);
      return of(Type.valueOf(type.toUpperCase()), domain, data);
    } catch (Exception ex) {
      throw new ApplicationRuntimeException(ex);
    }
  }

  public String getDomain() {
    return this.domain;
  }

  public Type getType() {
    return this.type;
  }

  public String getAction() {
    return String.format("%s.%s", this.domain, this.type);
  }

  public JSONObject getData() {
    return new JSONObject(this.data.toMap());
  }

  public boolean isDomainEvent() {
    return Type.CREATE.equals(this.type)
    || Type.UPDATE.equals(this.type)
    || Type.REMOVE.equals(this.type);
  }

  public JSONObject toJson() {
    var event = new JSONObject();
    event.put(EVENT, this.type.toString());
    event.put(DOMAIN, this.domain);
    event.put(DATA, this.data);
    return event;
  }
}
