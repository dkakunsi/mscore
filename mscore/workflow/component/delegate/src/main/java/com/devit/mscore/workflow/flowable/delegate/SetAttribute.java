package com.devit.mscore.workflow.flowable.delegate;

import static com.devit.mscore.ApplicationContext.getContext;
import static com.devit.mscore.ApplicationContext.setContext;
import static com.devit.mscore.util.AttributeConstants.getDomain;
import static com.devit.mscore.util.AttributeConstants.getId;
import static com.devit.mscore.web.WebUtils.getMessageType;
import static com.devit.mscore.web.WebUtils.SUCCESS;

import java.util.HashMap;
import java.util.Optional;

import com.devit.mscore.Logger;
import com.devit.mscore.exception.ApplicationRuntimeException;
import com.devit.mscore.exception.ProcessException;
import com.devit.mscore.exception.WebClientException;
import com.devit.mscore.logging.ApplicationLogger;
import com.devit.mscore.workflow.flowable.FlowableApplicationContext;

import org.flowable.common.engine.api.delegate.Expression;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.json.JSONObject;

public class SetAttribute implements JavaDelegate {

  private static final Logger LOGGER = ApplicationLogger.getLogger(SetAttribute.class);

  private Expression attribute;

  private Expression value;

  @Override
  public void execute(DelegateExecution execution) {
    var context = FlowableApplicationContext.of(execution);
    setContext(context);
    var domain = execution.getVariable("domain", String.class);
    var entityId = execution.getVariable("businessKey", String.class);

    var targetAttribute = this.attribute.getValue(execution).toString();
    var targetValue = this.value.getValue(execution).toString();

    LOGGER.info("Action: {}. Updating attribute {} of domain {} to {}.", context.getAction(), targetAttribute, domain,
        targetValue);

    var entity = getEntity(domain, entityId, targetAttribute, targetValue);
    entity.put(targetAttribute, targetValue);

    updateEntity(entity);

    execution.setVariable("entity", entity.toString());
    LOGGER.info("Entity process variable is updated.");
  }

  protected JSONObject getEntity(String domain, String entityId, String targetAttribute, String targetValue)
      throws ApplicationRuntimeException {
    var context = (FlowableApplicationContext) getContext();
    var dataClient = context.getDataClient();
    var client = dataClient.getClient();
    var uri = dataClient.getDomainUri(domain) + "/" + entityId;

    try {
      var entity = client.get(uri, new HashMap<>());
      if (entity == null || !isSuccess(entity.getInt("code"))) {
        LOGGER.error("Cannot update attribute {} to {}.", targetAttribute, targetValue);
        throw new ApplicationRuntimeException(new ProcessException("Cannot update status."));
      }
      var payload = entity.getJSONObject("payload");
      if (payload == null || payload.isEmpty()) {
        LOGGER.error("Cannot update attribute {} to {}. Entity is not found.", targetAttribute, targetValue);
        throw new ApplicationRuntimeException(new ProcessException("Cannot update status. Entity is not found."));
      }
      return payload;
    } catch (WebClientException ex) {
      LOGGER.error("Cannot update attribute {} to {}.", targetAttribute, targetValue);
      throw new ApplicationRuntimeException(ex);
    }
  }

  protected void updateEntity(JSONObject entity) {
    var context = (FlowableApplicationContext) getContext();
    var dataClient = context.getDataClient();
    var domain = getDomain(entity);
    var uri = String.format("%s/%s", dataClient.getDomainUri(domain), getId(entity));
    var client = dataClient.getClient();

    try {
      var response = client.put(uri, Optional.of(entity));
      if (response == null || !isSuccess(response.getInt("code"))) {
        LOGGER.error("Cannot update entity: {}", entity.getString("payload"));
        throw new ApplicationRuntimeException(new ProcessException("Cannot update entity."));
      }
    } catch (WebClientException ex) {
      LOGGER.error("Cannot update entity: {}", getId(entity));
      throw new ApplicationRuntimeException(ex);
    }
  }

  protected static boolean isSuccess(int code) {
    return getMessageType(code).equals(SUCCESS);
  }

  protected void setVariable(DelegateExecution execution, String variable, Object value) {
    execution.setVariable("entity", value.toString());
    LOGGER.info("Process variable {} is updated.", variable);
  }
}
