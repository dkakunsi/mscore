package com.devit.mscore.gateway.service;

import static com.devit.mscore.ApplicationContext.getContext;
import static com.devit.mscore.gateway.service.ServiceUtils.WEBCLIENT_EXCEPTION_MESSAGE;

import java.util.Optional;

import com.devit.mscore.Logger;
import com.devit.mscore.ServiceRegistration;
import com.devit.mscore.exception.ApplicationRuntimeException;
import com.devit.mscore.exception.WebClientException;
import com.devit.mscore.logging.ApplicationLogger;
import com.devit.mscore.web.Client;

import org.json.JSONObject;

public class WorkflowService extends AbstractGatewayService {

  private static final Logger LOGGER = ApplicationLogger.getLogger(WorkflowService.class);

  private static final String PROCESS = "process";

  private static final String DOMAIN = "api/workflow";

  public WorkflowService(ServiceRegistration serviceRegistration, Client client) {
    super(serviceRegistration, client);
  }

  @Override
  public String getDomain() {
    return DOMAIN;
  }

  public JSONObject createInstance(JSONObject payload) throws WebClientException {
    var context = getContext();
    try {
      var uri = String.format("%s/instance/%s", getUri(PROCESS), context.getAction());
      LOGGER.info(" Creating instance with action '{}'", context.getAction());
      return this.client.post(uri, Optional.of(payload));
    } catch (ApplicationRuntimeException ex) {
      throw new WebClientException(WEBCLIENT_EXCEPTION_MESSAGE, ex);
    }
  }

  public void completeTask(String taskId, JSONObject payload) throws WebClientException {
    try {
      var uri = String.format("%s/task/%s", getUri(PROCESS), taskId);
      LOGGER.info(" Completing task '{}'", taskId);
      this.client.put(uri, Optional.of(payload));
    } catch (ApplicationRuntimeException ex) {
      throw new WebClientException(WEBCLIENT_EXCEPTION_MESSAGE, ex);
    }
  }
}
