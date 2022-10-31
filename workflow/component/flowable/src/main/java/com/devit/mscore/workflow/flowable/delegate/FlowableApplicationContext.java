package com.devit.mscore.workflow.flowable.delegate;

import static com.devit.mscore.util.Utils.BREADCRUMB_ID;
import static com.devit.mscore.util.Utils.EVENT_TYPE;

import com.devit.mscore.ApplicationContext;
import com.devit.mscore.DataClient;
import com.devit.mscore.Event;
import com.devit.mscore.Publisher;

import java.util.HashMap;
import java.util.Map;

import org.flowable.engine.delegate.DelegateExecution;

public class FlowableApplicationContext extends ApplicationContext {

  private FlowableApplicationContext(Map<String, Object> contextData) {
    super(contextData);
  }

  @Override
  public String getSource() {
    return "flowable";
  }

  public static ApplicationContext of(DelegateExecution execution) {
    var contextData = new HashMap<String, Object>();
    var context = new FlowableApplicationContext(contextData);
    context.breadcrumbId(execution);
    context.eventType(execution);

    return context;
  }

  private void eventType(DelegateExecution execution) {
    var variableObj = execution.getVariable(EVENT_TYPE);
    if (variableObj != null) {
      setEventType(Event.Type.valueOf(variableObj.toString().toUpperCase()));
    }
  }

  private void breadcrumbId(DelegateExecution execution) {
    var variableObj = execution.getVariable(BREADCRUMB_ID);
    if (variableObj != null) {
      setBreadcrumbId(variableObj.toString());
    }
  }

  public DataClient getDataClient() {
    return DelegateUtils.getDataClient();
  }

  public Publisher getPublisher() {
    return DelegateUtils.getPublisher();
  }

  public String getChannel(String target) {
    return DelegateUtils.getChannel(target);
  }
}
