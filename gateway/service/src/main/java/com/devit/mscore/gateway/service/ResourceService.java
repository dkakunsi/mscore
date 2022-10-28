package com.devit.mscore.gateway.service;

import static com.devit.mscore.ApplicationContext.getContext;
import static com.devit.mscore.util.AttributeConstants.ID;
import static com.devit.mscore.web.WebUtils.SUCCESS;
import static com.devit.mscore.web.WebUtils.getMessageType;

import com.devit.mscore.Logger;
import com.devit.mscore.ServiceRegistration;
import com.devit.mscore.exception.ApplicationRuntimeException;
import com.devit.mscore.exception.WebClientException;
import com.devit.mscore.logging.ApplicationLogger;
import com.devit.mscore.web.Client;

import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.BooleanUtils;
import org.json.JSONObject;

public class ResourceService extends AbstractGatewayService {

  private static final Logger LOGGER = ApplicationLogger.getLogger(ResourceService.class);

  private final Boolean useWorkflow;

  private final WorkflowService workflowService;

  public ResourceService(ServiceRegistration serviceRegistration, Client client, WorkflowService workflowService,
      Boolean useWorkflow) {
    super(serviceRegistration, client);
    this.useWorkflow = useWorkflow;
    this.workflowService = workflowService;
  }

  @Override
  public String getDomain() {
    return "api/resource";
  }

  public JSONObject post(String domain, JSONObject payload) throws WebClientException {
    try {
      var uri = getUri(domain);
      var result = this.client.post(uri, Optional.of(payload));
      if (shouldExecuteWorkflow(result)) {
        LOGGER.info("Executing workflow");
        executeWorkflow(payload, result);
      }
      return result;
    } catch (ApplicationRuntimeException ex) {
      LOGGER.error("Cannot POST a resource");
      throw new WebClientException(WEBCLIENT_EXCEPTION_MESSAGE, ex);
    }
  }

  private boolean shouldExecuteWorkflow(JSONObject result) {
    var context = getContext();
    return BooleanUtils.isTrue(this.useWorkflow) && isSuccess(result.getInt("code")) && context.hasAction();
  }

  private boolean isSuccess(int responseCode) {
    return SUCCESS.equals(getMessageType(responseCode));
  }

  private void executeWorkflow(JSONObject payload, JSONObject result) {
    var resultId = result.getJSONObject(PAYLOAD).getString(ID);
    payload.put(ID, resultId);

    try {
      this.workflowService.createInstance(payload);
    } catch (WebClientException ex) {
      LOGGER.error("Cannot start a workflow instance", ex);
    }
  }

  public JSONObject put(String domain, String id, JSONObject payload) throws WebClientException {
    try {
      var uri = String.format("%s/%s", getUri(domain), id);
      return this.client.put(uri, Optional.of(payload));
    } catch (ApplicationRuntimeException ex) {
      throw new WebClientException(WEBCLIENT_EXCEPTION_MESSAGE, ex);
    }
  }

  public JSONObject getById(String domain, String id) throws WebClientException {
    try {
      var uri = String.format("%s/%s", getUri(domain), id);
      return this.client.get(uri, Map.of());
    } catch (ApplicationRuntimeException ex) {
      throw new WebClientException(WEBCLIENT_EXCEPTION_MESSAGE, ex);
    }
  }

  public JSONObject getByCode(String domain, String code) throws WebClientException {
    try {
      var uri = String.format("%s/code/%s", getUri(domain), code);
      return this.client.get(uri, null);
    } catch (ApplicationRuntimeException ex) {
      throw new WebClientException(WEBCLIENT_EXCEPTION_MESSAGE, ex);
    }
  }

  public JSONObject getMany(String domain, String ids) throws WebClientException {
    try {
      var uri = String.format("%s/keys", getUri(domain));
      return this.client.get(uri, Map.of("ids", ids));
    } catch (ApplicationRuntimeException ex) {
      throw new WebClientException(WEBCLIENT_EXCEPTION_MESSAGE, ex);
    }
  }

  public JSONObject sync(String domain) throws WebClientException {
    try {
      var uri = String.format("%s/sync", getUri(domain));
      return this.client.post(uri, Optional.empty());
    } catch (ApplicationRuntimeException ex) {
      throw new WebClientException(WEBCLIENT_EXCEPTION_MESSAGE, ex);
    }
  }

  public JSONObject syncById(String domain, String id) throws WebClientException {
    try {
      var uri = String.format("%s/%s/sync", getUri(domain), id);
      return this.client.post(uri, Optional.empty());
    } catch (ApplicationRuntimeException ex) {
      throw new WebClientException(WEBCLIENT_EXCEPTION_MESSAGE, ex);
    }
  }

  public JSONObject search(String domain, JSONObject criteria) throws WebClientException {
    try {
      var uri = String.format("%s/search", getUri(domain));
      return this.client.post(uri, Optional.of(criteria));
    } catch (ApplicationRuntimeException ex) {
      throw new WebClientException(WEBCLIENT_EXCEPTION_MESSAGE, ex);
    }
  }
}
