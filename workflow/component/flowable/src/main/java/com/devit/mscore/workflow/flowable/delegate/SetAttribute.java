package com.devit.mscore.workflow.flowable.delegate;

import static com.devit.mscore.ApplicationContext.setContext;
import static com.devit.mscore.util.AttributeConstants.getDomain;
import static com.devit.mscore.util.Constants.DOMAIN;
import static com.devit.mscore.util.Constants.ID;

import com.devit.mscore.Event;

import org.flowable.common.engine.api.delegate.Expression;
import org.flowable.engine.delegate.DelegateExecution;
import org.json.JSONObject;

public class SetAttribute extends SaveEntity {

  private static final String BUSINESS_KEY = "businessKey";

  private Expression attribute;

  private Expression value;

  @Override
  public void execute(DelegateExecution execution) {
    var targetAttribute = attribute.getValue(execution).toString();
    var targetValue = value.getValue(execution).toString();
    updateAttribute(execution, targetAttribute, targetValue);
  }

  protected void updateAttribute(DelegateExecution execution, String targetAttribute, String targetValue) {
    var context = FlowableApplicationContext.of(execution);
    setContext(context);

    var entity = getBaseEntity(execution);
    var domain = getDomain(entity);
    logger.info("Updating attribute '{}' of domain '{}' to '{}'", targetAttribute, domain, targetValue);

    entity.put(targetAttribute, targetValue);
    publishSaveEvent(entity, domain, Event.Type.UPDATE);
    logger.info("Update event is published");
  }

  protected JSONObject getBaseEntity(DelegateExecution execution) {
    var domain = execution.getVariable(DOMAIN, String.class);
    var entityId = execution.getVariable(BUSINESS_KEY, String.class);
    var entity = new JSONObject();
    entity.put(ID, entityId);
    entity.put(DOMAIN, domain);
    return entity;
  }
}
