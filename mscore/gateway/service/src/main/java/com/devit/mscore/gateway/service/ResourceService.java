package com.devit.mscore.gateway.service;

import static com.devit.mscore.gateway.service.ServiceUtils.PAYLOAD;
import static com.devit.mscore.gateway.service.ServiceUtils.WEBCLIENT_EXCEPTION_MESSAGE;
import static com.devit.mscore.util.AttributeConstants.ID;
import static com.devit.mscore.web.WebUtils.SUCCESS;

import static com.devit.mscore.web.WebUtils.getMessageType;

import java.util.Map;
import java.util.Optional;

import com.devit.mscore.ApplicationContext;
import com.devit.mscore.ServiceRegistration;
import com.devit.mscore.exception.ApplicationRuntimeException;
import com.devit.mscore.exception.WebClientException;
import com.devit.mscore.web.Client;

import org.apache.commons.lang3.BooleanUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceService extends AbstractGatewayService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceService.class);

    private final Boolean useWorkflow;

    private final WorkflowService workflowService;

    public ResourceService(ServiceRegistration serviceRegistration, Client client, WorkflowService workflowService, Boolean useWorkflow) {
        super(serviceRegistration, client);
        this.useWorkflow = useWorkflow;
        this.workflowService = workflowService;
    }

    @Override
    public String getDomain() {
        return "api/resource";
    }

    public JSONObject post(ApplicationContext context, String domain, JSONObject payload) throws WebClientException {
        try {
            var uri = getUri(context, domain);
            var result = this.client.post(context, uri, Optional.of(payload));
            if (BooleanUtils.isTrue(this.useWorkflow) && isSuccess(result.getInt("code"))) {
                LOGGER.info("BreadcrumbId: {}. Executing workflow.", context.getBreadcrumbId());
                executeWorkflow(context, payload, result);
            }
            return result;
        } catch (ApplicationRuntimeException ex) {
            LOGGER.error("BreadcrumbId: {}. Cannot POST a resource.", context.getBreadcrumbId());
            throw new WebClientException(WEBCLIENT_EXCEPTION_MESSAGE, ex);
        }
    }

    private boolean isSuccess(int responseCode) {
        return SUCCESS.equals(getMessageType(responseCode));
    }

    private void executeWorkflow(ApplicationContext context, JSONObject payload, JSONObject result) {
        var resultId = result.getJSONObject(PAYLOAD).getString(ID);
        payload.put(ID, resultId);

        try {
            this.workflowService.createInstance(context, payload);
        } catch (WebClientException ex) {
            LOGGER.error("BreadcrumbId: {}. Cannot start a workflow instance.", context.getBreadcrumbId(), ex);
        }
    }

    public JSONObject put(ApplicationContext context, String domain, String id, JSONObject payload) throws WebClientException {
        try {
            var uri = String.format("%s/%s", getUri(context, domain), id);
            return this.client.put(context, uri, Optional.of(payload));
        } catch (ApplicationRuntimeException ex) {
            throw new WebClientException(WEBCLIENT_EXCEPTION_MESSAGE, ex);
        }
    }

    public JSONObject getById(ApplicationContext context, String domain, String id) throws WebClientException {
        try {
            var uri = String.format("%s/%s", getUri(context, domain), id);
            return this.client.get(context, uri, Map.of());
        } catch (ApplicationRuntimeException ex) {
            throw new WebClientException(WEBCLIENT_EXCEPTION_MESSAGE, ex);
        }
    }

    public JSONObject getByCode(ApplicationContext context, String domain, String code) throws WebClientException {
        try {
            var uri = String.format("%s/code/%s", getUri(context, domain), code);
            return this.client.get(context, uri, null);
        } catch (ApplicationRuntimeException ex) {
            throw new WebClientException(WEBCLIENT_EXCEPTION_MESSAGE, ex);
        }
    }

    public JSONObject getMany(ApplicationContext context, String domain, String ids) throws WebClientException {
        try {
            var uri = String.format("%s/keys", getUri(context, domain));
            return this.client.get(context, uri, Map.of("ids", ids));
        } catch (ApplicationRuntimeException ex) {
            throw new WebClientException(WEBCLIENT_EXCEPTION_MESSAGE, ex);
        }
    }

    public JSONObject sync(ApplicationContext context, String domain) throws WebClientException {
        try {
            var uri = String.format("%s/sync", getUri(context, domain));
            return this.client.post(context, uri, Optional.empty());
        } catch (ApplicationRuntimeException ex) {
            throw new WebClientException(WEBCLIENT_EXCEPTION_MESSAGE, ex);
        }
    }

    public JSONObject syncById(ApplicationContext context, String domain, String id) throws WebClientException {
        try {
            var uri = String.format("%s/%s/sync", getUri(context, domain), id);
            return this.client.post(context, uri, Optional.empty());
        } catch (ApplicationRuntimeException ex) {
            throw new WebClientException(WEBCLIENT_EXCEPTION_MESSAGE, ex);
        }
    }

    public JSONObject search(ApplicationContext context, String domain, JSONObject criteria) throws WebClientException {
        try {
            var uri = String.format("%s/search", getUri(context, domain));
            return this.client.post(context, uri, Optional.of(criteria));
        } catch (ApplicationRuntimeException ex) {
            throw new WebClientException(WEBCLIENT_EXCEPTION_MESSAGE, ex);
        }
    }
}
