package com.devit.mscore.workflow.flowable.delegate;

import static com.devit.mscore.util.AttributeConstants.getDomain;
import static com.devit.mscore.util.AttributeConstants.getId;
import static com.devit.mscore.web.WebUtils.getMessageType;
import static com.devit.mscore.web.WebUtils.SUCCESS;

import java.util.HashMap;
import java.util.Optional;

import com.devit.mscore.ApplicationContext;
import com.devit.mscore.exception.ApplicationRuntimeException;
import com.devit.mscore.exception.ProcessException;
import com.devit.mscore.exception.WebClientException;
import com.devit.mscore.workflow.flowable.FlowableApplicationContext;

import org.flowable.common.engine.api.delegate.Expression;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SetAttribute implements JavaDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(SetAttribute.class);

    private Expression attribute;

    private Expression value;

    @Override
    public void execute(DelegateExecution execution) {
        var context = FlowableApplicationContext.of(execution);
        var domain = execution.getVariable("domain", String.class);
        var entityId = execution.getVariable("businessKey", String.class);

        var targetAttribute = this.attribute.getValue(execution).toString();
        var targetValue = this.value.getValue(execution).toString();

        LOGGER.info("BreadcrumbId: {}. Action: {}. Updating attribute {} of domain {} to {}.",
                context.getBreadcrumbId(), context.getAction(), targetAttribute, domain, targetValue);

        var entity = getEntity((FlowableApplicationContext) context, domain, entityId, targetAttribute, targetValue);
        entity.put(targetAttribute, targetValue);

        updateEntity((FlowableApplicationContext) context, entity);

        execution.setVariable("entity", entity.toString());
        LOGGER.info("BreadcrumbId: {}. Entity process variable is updated.", context.getBreadcrumbId());
    }

    protected JSONObject getEntity(FlowableApplicationContext context, String domain, String entityId, String targetAttribute, String targetValue) throws ApplicationRuntimeException {
        var dataClient = context.getDataClient();
        var client = dataClient.getClient();
        var uri = dataClient.getDomainUri(context, domain) + "/" + entityId;

        try {
            var entity = client.get(context, uri, new HashMap<>());
            if (entity == null || !isSuccess(entity.getInt("code"))) {
                LOGGER.error("BreadcrumbId: {}. Cannot update attribute {} to {}.", context.getBreadcrumbId(), targetAttribute, targetValue);
                throw new ApplicationRuntimeException(new ProcessException("Cannot update status."));
            }
            var payload = entity.getJSONObject("payload");
            if (payload == null || payload.isEmpty()) {
                LOGGER.error("BreadcrumbId: {}. Cannot update attribute {} to {}. Entity is not found.", context.getBreadcrumbId(), targetAttribute, targetValue);
                throw new ApplicationRuntimeException(new ProcessException("Cannot update status. Entity is not found."));
            }
            return payload;
        } catch (WebClientException ex) {
            LOGGER.error("BreadcrumbId: {}. Cannot update attribute {} to {}.", context.getBreadcrumbId(), targetAttribute, targetValue);
            throw new ApplicationRuntimeException(ex);
        }
    }

    protected void updateEntity(FlowableApplicationContext context, JSONObject entity) {
        var dataClient = context.getDataClient();
        var domain = getDomain(entity);
        var uri = String.format("%s/%s", dataClient.getDomainUri(context, domain), getId(entity));
        var client = dataClient.getClient();

        try {
            var response = client.put(context, uri, Optional.of(entity));
            if (response == null || !isSuccess(response.getInt("code"))) {
                LOGGER.error("BreadcrumbId: {}. Cannot update entity: {}", context.getBreadcrumbId(), entity.getString("payload"));
                throw new ApplicationRuntimeException(new ProcessException("Cannot update entity."));
            }
        } catch (WebClientException ex) {
            LOGGER.error("BreadcrumbId: {}. Cannot update entity: {}", context.getBreadcrumbId(), getId(entity));
            throw new ApplicationRuntimeException(ex);
        }
    }

    protected static boolean isSuccess(int code) {
        return getMessageType(code).equals(SUCCESS);
    }

    protected void setVariable(DelegateExecution execution, ApplicationContext context, String variable, Object value) {
        execution.setVariable("entity", value.toString());
        LOGGER.info("BreadcrumbId: {}. Process variable {} is updated.", context.getBreadcrumbId(), variable);
    }
}
