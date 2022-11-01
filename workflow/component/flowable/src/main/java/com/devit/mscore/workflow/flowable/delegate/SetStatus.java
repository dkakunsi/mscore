package com.devit.mscore.workflow.flowable.delegate;

import static com.devit.mscore.ApplicationContext.setContext;

import com.devit.mscore.Logger;
import com.devit.mscore.logging.ApplicationLogger;
import com.devit.mscore.util.AttributeConstants;

import org.apache.commons.lang3.StringUtils;
import org.flowable.common.engine.api.delegate.Expression;
import org.flowable.engine.delegate.DelegateExecution;
import org.json.JSONObject;

public class SetStatus extends SetAttribute {

  private static final Logger LOGGER = ApplicationLogger.getLogger(SetStatus.class);

  private Expression status;

  private Expression closeReason;

  @Override
  public void execute(DelegateExecution execution) {
    var context = FlowableApplicationContext.of(execution);
    setContext(context);

    var domain = getEntityDomain(execution);
    var entityId = execution.getVariable("businessKey", String.class);
    var targetValue = this.status.getValue(execution).toString();

    LOGGER.info("Updating status of '{}' in domain '{}' to '{}'", entityId, domain, targetValue);

    var entity = getEntity(domain, entityId, "status", targetValue);
    entity.put("status", targetValue);

    var closeReasonStr = getCloseReason(execution);
    if (StringUtils.isNotBlank(closeReasonStr)) {
      entity.put("closeReason", closeReasonStr);
    }

    updateEntity(entity);

    execution.setVariable("entity", entity.toString());
    LOGGER.info("BreadcrumbId: {}. Entity process variable is updated", context.getBreadcrumbId());
  }

  private String getCloseReason(DelegateExecution execution) {
    try {
      return this.closeReason.getValue(execution).toString();
    } catch (NullPointerException npe) {
      // get closeReason from variable.
      return execution.getVariable("closeReason", String.class);
    }
  }

  private static String getEntityDomain(DelegateExecution execution) {
    var entityStr = execution.getVariable("entity", String.class);
    var entityObj = new JSONObject(entityStr);
    return AttributeConstants.getDomain(entityObj);
  }
}
