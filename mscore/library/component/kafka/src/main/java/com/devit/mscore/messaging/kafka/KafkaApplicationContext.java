package com.devit.mscore.messaging.kafka;

import static com.devit.mscore.util.Utils.ACTION;
import static com.devit.mscore.util.Utils.BREADCRUMB_ID;
import static com.devit.mscore.util.Utils.PRINCIPAL;

import java.util.HashMap;
import java.util.Map;

import com.devit.mscore.ApplicationContext;

import org.apache.kafka.common.header.Headers;

public class KafkaApplicationContext extends ApplicationContext {

  private KafkaApplicationContext(Map<String, Object> contextData) {
    super(contextData);
  }

  public static ApplicationContext of(Headers headers) {
    var contextData = new HashMap<String, Object>();
    var context = new KafkaApplicationContext(contextData);
    context.setPrincipalIfExists(headers);
    context.setBreadcrumbIdIfExistsOrGenerate(headers);
    context.setActionIfExists(headers);

    return context;
  }

  private void setPrincipalIfExists(Headers headers) {
    var principalHeader = headers.lastHeader(PRINCIPAL);
    if (principalHeader != null) {
      setPrincipal(new String(principalHeader.value()));
    }
  }

  private void setBreadcrumbIdIfExistsOrGenerate(Headers headers) {
    var breadcrumbIdHeader = headers.lastHeader(BREADCRUMB_ID);
    if (breadcrumbIdHeader != null) {
      setBreadcrumbId(new String(breadcrumbIdHeader.value()));
    } else {
      generateBreadcrumbId();
    }
  }

  private void setActionIfExists(Headers headers) {
    var actionHeader = headers.lastHeader(ACTION);
    if (actionHeader != null) {
      this.contextData.put(ACTION, new String(actionHeader.value()));
    }
  }

  @Override
  public String getSource() {
    return "messaging";
  }
}
