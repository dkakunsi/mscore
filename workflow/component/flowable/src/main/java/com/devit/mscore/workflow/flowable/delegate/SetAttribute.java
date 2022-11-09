package com.devit.mscore.workflow.flowable.delegate;

import static com.devit.mscore.ApplicationContext.getContext;
import static com.devit.mscore.ApplicationContext.setContext;
import static com.devit.mscore.util.AttributeConstants.getDomain;
import static com.devit.mscore.util.Constants.DOMAIN;
import static com.devit.mscore.web.WebUtils.SUCCESS;
import static com.devit.mscore.web.WebUtils.getMessageType;

import com.devit.mscore.Event;
import com.devit.mscore.Logger;
import com.devit.mscore.exception.ApplicationRuntimeException;
import com.devit.mscore.exception.ProcessException;
import com.devit.mscore.exception.WebClientException;
import com.devit.mscore.logging.ApplicationLogger;

import java.util.HashMap;

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

    var targetAttribute = attribute.getValue(execution).toString();
    var targetValue = value.getValue(execution).toString();

    LOGGER.info("Updating attribute '{}' of domain '{}' to '{}'", targetAttribute, domain, targetValue);

    var entity = getEntityFromData(domain, entityId, targetAttribute, targetValue);
    entity.put(targetAttribute, targetValue);

    updateEntity(entity);

    execution.setVariable("entity", entity.toString());
    LOGGER.info("Entity process variable is updated");
  }

  protected JSONObject getEntityFromData(String domain, String entityId, String targetAttribute, String targetValue)
      throws ApplicationRuntimeException {
    var context = (FlowableApplicationContext) getContext();
    var dataClient = context.getDataClient();
    var client = dataClient.getClient();
    var uri = dataClient.getDomainUri(domain) + "/" + entityId;

    try {
      var entity = client.get(uri, new HashMap<>());
      if (entity == null || !isSuccess(entity.getInt("code"))) {
        LOGGER.error("Cannot update attribute '{}' to '{}'", targetAttribute, targetValue);
        throw new ApplicationRuntimeException(new ProcessException("Cannot update status"));
      }
      var payload = entity.getJSONObject("payload");
      if (payload == null || payload.isEmpty()) {
        LOGGER.error("Cannot update attribute '{}' to '{}'. Entity is not found", targetAttribute, targetValue);
        throw new ApplicationRuntimeException(new ProcessException("Cannot update status. Entity is not found"));
      }
      return payload;
    } catch (WebClientException ex) {
      LOGGER.error("Cannot update attribute '{}' to '{}'", targetAttribute, targetValue);
      throw new ApplicationRuntimeException(ex);
    }
  }

  protected void updateEntity(JSONObject entity) {
    var context = (FlowableApplicationContext) getContext();
    var domain = getDomain(entity);
    var event = Event.of(Event.Type.UPDATE, domain, context.getAction().orElse(null), entity);

    var publisher = context.getPublisher();
    var eventChannel = context.getChannel(DOMAIN);
    publisher.publish(eventChannel, event.toJson());
  }

  protected static boolean isSuccess(int code) {
    return getMessageType(code).equals(SUCCESS);
  }

  protected void setVariable(DelegateExecution execution, String variable, Object value) {
    execution.setVariable("entity", value.toString());
    LOGGER.info("Process variable '{}' is updated", variable);
  }
}
