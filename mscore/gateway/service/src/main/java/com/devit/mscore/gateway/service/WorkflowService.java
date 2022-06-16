package com.devit.mscore.gateway.service;

import static com.devit.mscore.gateway.service.ServiceUtils.WEBCLIENT_EXCEPTION_MESSAGE;

import java.util.Optional;

import com.devit.mscore.ApplicationContext;
import com.devit.mscore.ServiceRegistration;
import com.devit.mscore.exception.ApplicationRuntimeException;
import com.devit.mscore.exception.WebClientException;
import com.devit.mscore.web.Client;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkflowService extends AbstractGatewayService {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkflowService.class);

    private static final String PROCESS = "process";

    private static final String DOMAIN = "api/workflow";

    public WorkflowService(ServiceRegistration serviceRegistration, Client client) {
        super(serviceRegistration, client);
    }

    @Override
    public String getDomain() {
        return DOMAIN;
    }

    public JSONObject createInstance(ApplicationContext context, JSONObject payload) throws WebClientException {
        try {
            var uri = String.format("%s/instance/%s", getUri(context, PROCESS), context.getAction());
            LOGGER.info("BreadcrumbdId: {}. Creating instance with action '{}'", context.getBreadcrumbId(), context.getAction());
            return this.client.post(context, uri, Optional.of(payload));
        } catch (ApplicationRuntimeException ex) {
            throw new WebClientException(WEBCLIENT_EXCEPTION_MESSAGE, ex);
        }
    }

    public void completeTask(ApplicationContext context, String taskId, JSONObject payload) throws WebClientException {
        try {
            var uri = String.format("%s/task/%s", getUri(context, PROCESS), taskId);
            LOGGER.info("BreadcrumbId: {}. Completing task '{}'", context.getBreadcrumbId(), taskId);
            this.client.put(context, uri, Optional.of(payload));
        } catch (ApplicationRuntimeException ex) {
            throw new WebClientException(WEBCLIENT_EXCEPTION_MESSAGE, ex);
        }
    }
}
