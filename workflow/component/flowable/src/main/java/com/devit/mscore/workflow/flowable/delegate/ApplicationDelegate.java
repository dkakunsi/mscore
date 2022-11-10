package com.devit.mscore.workflow.flowable.delegate;

import static com.devit.mscore.ApplicationContext.getContext;
import static com.devit.mscore.ApplicationContext.setContext;
import static com.devit.mscore.WorkflowConstants.BUSINESS_KEY;
import static com.devit.mscore.util.AttributeConstants.CREATED_BY;
import static com.devit.mscore.util.AttributeConstants.NAME;
import static com.devit.mscore.util.Constants.ACTION;
import static com.devit.mscore.util.Constants.BREADCRUMB_ID;
import static com.devit.mscore.util.Constants.DOMAIN;
import static com.devit.mscore.util.Constants.ENTITY;
import static com.devit.mscore.util.Constants.EVENT_TYPE;
import static com.devit.mscore.util.Constants.ID;
import static com.devit.mscore.util.Constants.PRINCIPAL;

import com.devit.mscore.Event;
import com.devit.mscore.Logger;
import com.devit.mscore.logging.ApplicationLogger;
import com.devit.mscore.workflow.flowable.delegate.exception.ExpressionNotProvidedException;
import com.devit.mscore.workflow.flowable.delegate.exception.VariableNotFoundException;

import org.apache.commons.lang3.StringUtils;
import org.flowable.common.engine.api.delegate.Expression;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.json.JSONObject;

public abstract class ApplicationDelegate implements JavaDelegate {

  protected Logger logger = ApplicationLogger.getLogger(getClass());
  
  protected Expression value;

  public void setValue(Expression value) {
    this.value = value;
  }

  protected DelegateApplicationContext initContext(DelegateExecution execution) {
    var context = (DelegateApplicationContext) DelegateApplicationContext.of(execution);
    setContext(context);
    return context;
  }

  protected String getValue(DelegateExecution execution) {
    return getValue(execution, value);
  }

  protected static String getValue(DelegateExecution execution, Expression expression) {
    if (expression == null) {
      throw new ExpressionNotProvidedException("Expression is not provided");
    }
    var expressionValue = expression.getValue(execution);
    if (expressionValue == null) {
      throw new ExpressionNotProvidedException("The expression value is null");
    }
    return expressionValue.toString();
  }

  protected String getProcessVariable(DelegateExecution execution, String variableName) {
    var variable = execution.getVariable(variableName, String.class);
    if (StringUtils.isBlank(variable)) {
      logger.warn("Variable '{}' is not available in process variable", variableName);
      throw new VariableNotFoundException(String.format("Variable %s is not available in process variable", variableName));
    }
    return variable;    
  }

  protected JSONObject getBaseEntity(DelegateExecution execution) {
    var entity = new JSONObject();
    entity.put(ID, getBusinessKeyVariable(execution));
    entity.put(DOMAIN, getDomainVariable(execution));
    return entity;
  }

  protected void publishSaveEvent(JSONObject entity, String domain, String action, Event.Type eventType) {
    var context = (DelegateApplicationContext) getContext();
    var event = Event.of(eventType, domain, action, entity);

    var publisher = context.getPublisher();
    var eventChannel = context.getChannel(DOMAIN);
    publisher.publish(eventChannel, event.toJson());
  }

  protected String getDomainVariable(DelegateExecution execution) {
    return getProcessVariable(execution, DOMAIN);
  }

  protected String getEntityVariable(DelegateExecution execution) {
    return getProcessVariable(execution, ENTITY);
  }

  protected JSONObject getEntityObject(DelegateExecution execution) {
    return new JSONObject(getEntityVariable(execution));
  }

  protected String getBusinessKeyVariable(DelegateExecution execution) {
    return getProcessVariable(execution, BUSINESS_KEY);
  }

  protected String getNameVariable(DelegateExecution execution) {
    return getProcessVariable(execution, NAME);
  }

  protected String getBreadcrumbIdVariable(DelegateExecution execution) {
    return getProcessVariable(execution, BREADCRUMB_ID);
  }

  protected String getCreatedByVariable(DelegateExecution execution) {
    return getProcessVariable(execution, CREATED_BY);
  }

  protected String getEventTypeVariable(DelegateExecution execution) {
    return getProcessVariable(execution, EVENT_TYPE);
  }

  protected String getPrincipalVariable(DelegateExecution execution) {
    return getProcessVariable(execution, PRINCIPAL);
  }

  protected JSONObject getPrincipalObject(DelegateExecution execution) {
    return new JSONObject(getPrincipalVariable(execution));
  }

  protected String getActionVariable(DelegateExecution execution) {
    return getProcessVariable(execution, ACTION);
  }
}
