package com.devit.mscore.workflow.flowable.delegate;

import static com.devit.mscore.ApplicationContext.getContext;
import static com.devit.mscore.util.AttributeConstants.getDomain;
import static com.devit.mscore.util.Constants.DOMAIN;
import static com.devit.mscore.util.Constants.ENTITY;

import com.devit.mscore.Event;
import com.devit.mscore.Logger;
import com.devit.mscore.logging.ApplicationLogger;

import org.flowable.common.engine.api.delegate.Expression;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.json.JSONObject;

public class SaveEntity implements JavaDelegate {

  protected final Logger logger = ApplicationLogger.getLogger(getClass());
  
  private Expression update;

  private boolean isUpdate(DelegateExecution execution) {
    if (update == null) {
      return false;
    }
    var strValue = update.getValue(execution);
    if (strValue == null) {
      return false;
    }
    return Boolean.parseBoolean(strValue.toString());
  }

  protected void publishSaveEvent(JSONObject entity, String domain, Event.Type eventType) {
    var context = (FlowableApplicationContext) getContext();
    var event = Event.of(eventType, domain, context.getAction().orElse(null), entity);

    var publisher = context.getPublisher();
    var eventChannel = context.getChannel(DOMAIN);
    publisher.publish(eventChannel, event.toJson());
  }

  protected JSONObject getEntity(DelegateExecution execution) {
    var strEntity = (String) execution.getVariable(ENTITY);
    return new JSONObject(strEntity);
  }

  @Override
  public void execute(DelegateExecution execution) {
    var entity = getEntity(execution);
    var domain = getDomain(entity);
    var eventType = isUpdate(execution) ? Event.Type.UPDATE : Event.Type.CREATE;
    publishSaveEvent(entity, domain, eventType);    
  }
}
