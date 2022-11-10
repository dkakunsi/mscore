package com.devit.mscore.workflow.flowable.delegate;

import com.devit.mscore.Event;

import org.flowable.common.engine.api.delegate.Expression;
import org.flowable.engine.delegate.DelegateExecution;

public class SetAttribute extends ApplicationDelegate {

  Expression attribute;

  @Override
  public void execute(DelegateExecution execution) {
    initContext(execution);

    var targetAttribute = getValue(execution, attribute);
    var targetValue = getValue(execution);
    updateAttribute(execution, targetAttribute, targetValue);
  }

  protected void updateAttribute(DelegateExecution execution, String targetAttribute, String targetValue) {
    var entity = getBaseEntity(execution);
    var domain = getDomainVariable(execution);
    logger.info("Updating attribute '{}' of domain '{}' to '{}'", targetAttribute, domain, targetValue);

    entity.put(targetAttribute, targetValue);
    var action = domain + ".update";
    publishSaveEvent(entity, domain, action, Event.Type.UPDATE);
    logger.info("Update event is published");
  }

  public void setAttribute(Expression attribute) {
    this.attribute = attribute;
  }
}
