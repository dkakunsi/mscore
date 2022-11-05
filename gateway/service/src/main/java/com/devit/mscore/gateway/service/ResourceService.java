package com.devit.mscore.gateway.service;

import com.devit.mscore.Resource;
import com.devit.mscore.ServiceRegistration;
import com.devit.mscore.exception.ApplicationRuntimeException;
import com.devit.mscore.exception.WebClientException;
import com.devit.mscore.web.Client;

import java.util.Map;
import java.util.Optional;

import javax.naming.OperationNotSupportedException;

import org.json.JSONObject;

public class ResourceService extends AbstractGatewayService {

  public ResourceService(ServiceRegistration serviceRegistration, Client client) {
    super(serviceRegistration, client);
  }

  @Override
  public String getDomain() {
    return "api/resource";
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

  @Override
  public Resource getSchema() {
    throw new RuntimeException(new OperationNotSupportedException());
  }
}
