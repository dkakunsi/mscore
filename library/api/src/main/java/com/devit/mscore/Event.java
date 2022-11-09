package com.devit.mscore;

import static com.devit.mscore.util.Constants.ACTION;
import static com.devit.mscore.util.Constants.DATA;
import static com.devit.mscore.util.Constants.DOMAIN;
import static com.devit.mscore.util.Constants.EVENT;
import static com.devit.mscore.util.Constants.VARIABLE;

import com.devit.mscore.exception.ApplicationRuntimeException;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

public class Event {

  private Type type;

  private String domain;

  private String action;

  private JSONObject data;

  private JSONObject variables;

  protected Event(Type type, String domain, String action, JSONObject data, JSONObject variables) {
    this.type = type;
    this.domain = domain;
    this.data = data;
    this.action = action;
    this.variables = variables;
  }

  public static Event of(Type type, String domain, String action, JSONObject data, JSONObject variables) {
    if (StringUtils.isBlank(action)) {
      action = generateAction(domain, type);
    }
    return new Event(type, domain, action, data, variables);
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
      var variables = event.optJSONObject(VARIABLE);
      return of(Type.valueOf(type.toUpperCase()), domain, action, data, variables);
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

  public JSONObject getVariables() {
    return variables;
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

  public static enum Type {
    CREATE("create"),
    UPDATE("update"),
    REMOVE("remove"),
    TASK("task");

    private String name;

    private Type(String name) {
      this.name = name;
    }

    @Override
    public String toString() {
      return this.name;
    }
  }
}
