package com.devit.mscore.workflow.flowable.delegate;

import com.devit.mscore.Event;
import com.devit.mscore.workflow.flowable.delegate.exception.ExpressionNotProvidedException;

import org.flowable.common.engine.api.delegate.Expression;
import org.flowable.engine.delegate.DelegateExecution;

public class SaveEntity extends ApplicationDelegate {
  
  Expression update;

  private boolean isUpdate(DelegateExecution execution) {
    try {
      var strValue = getValue(execution, update);
      return Boolean.parseBoolean(strValue);
    } catch (ExpressionNotProvidedException ex) {
      return false;
    }
  }

  @Override
  public void execute(DelegateExecution execution) {
    var context = initContext(execution);

    logger.info("Saving entity");

    var entity = getEntityObject(execution);
    var domain = getDomainVariable(execution);
    var action = context.getAction().orElse(null);
    var eventType = isUpdate(execution) ? Event.Type.UPDATE : Event.Type.CREATE;
    publishSaveEvent(entity, domain, action, eventType);    
  }

  public void setUpdate(Expression update) {
    this.update = update;
  }
}
