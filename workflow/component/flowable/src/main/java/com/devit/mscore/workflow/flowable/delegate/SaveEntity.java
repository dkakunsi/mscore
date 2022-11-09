package com.devit.mscore.workflow.flowable.delegate;

import static com.devit.mscore.ApplicationContext.getContext;
import static com.devit.mscore.util.AttributeConstants.getDomain;
import static com.devit.mscore.util.Constants.DOMAIN;
import static com.devit.mscore.util.Constants.ENTITY;

import com.devit.mscore.Event;

import org.flowable.common.engine.api.delegate.Expression;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.json.JSONObject;

public class SaveEntity implements JavaDelegate {
  
  private Expression update;

  private boolean isUpdate(DelegateExecution execution) {
    var strValue = update.getValue(execution);
    if (strValue == null) {
      return false;
    }
    return Boolean.parseBoolean(strValue.toString());
  }

  private void publishSaveEvent(JSONObject entity, Event.Type eventType) {
    var context = (FlowableApplicationContext) getContext();
    var domain = getDomain(entity);
    var event = Event.of(eventType, domain, context.getAction().orElse(null), entity);

    var publisher = context.getPublisher();
    var eventChannel = context.getChannel(DOMAIN);
    publisher.publish(eventChannel, event.toJson());
  }

  private JSONObject getEntity(DelegateExecution execution) {
    var strEntity = (String) execution.getVariable(ENTITY);
    return new JSONObject(strEntity);
  }

  @Override
  public void execute(DelegateExecution execution) {
    var entity = getEntity(execution);
    var eventType = isUpdate(execution) ? Event.Type.UPDATE : Event.Type.CREATE;
    publishSaveEvent(entity, eventType);    
  }
}
