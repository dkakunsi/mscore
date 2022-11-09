package com.devit.mscore.web.javalin;

import static com.devit.mscore.util.Constants.ACTION;
import static com.devit.mscore.util.Constants.AUTHORIZATION;
import static com.devit.mscore.util.Constants.BREADCRUMB_ID;
import static com.devit.mscore.util.Constants.EVENT_TYPE;

import com.devit.mscore.ApplicationContext;
import com.devit.mscore.Event;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import io.javalin.http.Context;

public class JavalinApplicationContext extends ApplicationContext {

  private JavalinApplicationContext(Map<String, Object> contextData) {
    super(contextData);
  }

  public static ApplicationContext of(ApplicationContext appContext, Map<String, Object> contextData) {
    var newContextData = new HashMap<String, Object>();
    if (appContext != null) {
      newContextData.putAll(appContext.getContextData());
    }

    if (contextData != null) {
      newContextData.putAll(contextData);
    }

    return new JavalinApplicationContext(newContextData);
  }
  
  public static ApplicationContext of(Context ctx, Map<String, Object> contextData) {
    contextData.putAll(ctx.headerMap());

    var context = new JavalinApplicationContext(contextData);
    context.breadcrumbId(ctx);
    context.authorization(ctx);
    context.eventType(ctx);
    context.action(ctx);

    return context;
  }
  
  public static ApplicationContext of(Context ctx) {
    var contextData = new HashMap<String, Object>();
    return of(ctx, contextData);
  }

  private String getValue(Context ctx, String key) {
    var value = ctx.attribute(key);
    if (value == null || StringUtils.isBlank(value.toString())) {
      value = ctx.header(key);
    }
    return value != null ? value.toString() : null;
  }

  private void breadcrumbId(Context ctx) {
    var breadcrumbId = getValue(ctx, BREADCRUMB_ID);
    if (StringUtils.isNotBlank(breadcrumbId)) {
      setBreadcrumbId(breadcrumbId);
    }
  }

  private void eventType(Context ctx) {
    var eventType = ctx.header(EVENT_TYPE);
    if (StringUtils.isNotBlank(eventType)) {
      setEventType(Event.Type.valueOf(eventType.toUpperCase()));
    }
  }

  private void authorization(Context ctx) {
    var authorization = ctx.header(AUTHORIZATION);
    if (StringUtils.isNotBlank(authorization)) {
      contextData.put(AUTHORIZATION, authorization);
    }
  }

  private void action(Context ctx) {
    var action = ctx.header(ACTION);
    if (StringUtils.isNotBlank(action)) {
      setAction(action);
    }
  }

  @Override
  public String getSource() {
    return "web";
  }
}
