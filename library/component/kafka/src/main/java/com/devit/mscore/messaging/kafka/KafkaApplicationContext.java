package com.devit.mscore.messaging.kafka;

import static com.devit.mscore.util.Utils.BREADCRUMB_ID;
import static com.devit.mscore.util.Utils.EVENT_TYPE;
import static com.devit.mscore.util.Utils.PRINCIPAL;

import com.devit.mscore.ApplicationContext;
import com.devit.mscore.Event;
import com.devit.mscore.exception.ApplicationRuntimeException;
import com.devit.mscore.exception.ConfigException;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.common.header.Headers;

public class KafkaApplicationContext extends ApplicationContext {

  private KafkaApplicationContext(Map<String, Object> contextData) {
    super(contextData);
  }

  public static ApplicationContext of(Headers headers, Map<String, Object> contextData) {
    var context = new KafkaApplicationContext(contextData);

    try {
      context.setPrincipalIfExists(headers);
      context.setBreadcrumbIdIfExistsOrGenerate(headers);
      context.setEventTypeIfExists(headers);
    } catch (UnsupportedEncodingException ex) {
      throw new ApplicationRuntimeException(new ConfigException(ex));
    }

    return context;
  }

  public static ApplicationContext of(Headers headers) {
    var contextData = new HashMap<String, Object>();
    return of(headers, contextData);
  }

  private void setPrincipalIfExists(Headers headers) throws UnsupportedEncodingException {
    var principalHeader = headers.lastHeader(PRINCIPAL);
    if (principalHeader != null) {
      setPrincipal(new String(principalHeader.value(), StandardCharsets.UTF_8.name()));
    }
  }

  private void setBreadcrumbIdIfExistsOrGenerate(Headers headers) throws UnsupportedEncodingException {
    var breadcrumbIdHeader = headers.lastHeader(BREADCRUMB_ID);
    if (breadcrumbIdHeader != null) {
      setBreadcrumbId(new String(breadcrumbIdHeader.value(), StandardCharsets.UTF_8.name()));
    } else {
      generateBreadcrumbId();
    }
  }

  private void setEventTypeIfExists(Headers headers) throws UnsupportedEncodingException {
    var eventTypeHeader = headers.lastHeader(EVENT_TYPE);
    if (eventTypeHeader != null) {
      var eventType = new String(eventTypeHeader.value(), StandardCharsets.UTF_8.name());
      setEventType(Event.Type.valueOf(eventType.toUpperCase()));
    }
  }

  @Override
  public String getSource() {
    return "messaging";
  }
}
