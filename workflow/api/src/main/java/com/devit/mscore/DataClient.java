package com.devit.mscore;

import com.devit.mscore.exception.ApplicationRuntimeException;
import com.devit.mscore.exception.RegistryException;
import com.devit.mscore.web.Client;

import java.util.HashMap;
import java.util.Map;

public class DataClient {

  private final Client client;

  private final ServiceRegistration serviceRegistration;

  private final String workflowDomain;

  private Map<String, String> uriCache;

  public DataClient(Client client, ServiceRegistration serviceRegistration, String workflowDomain) {
    this.workflowDomain = workflowDomain;
    this.uriCache = new HashMap<>();
    this.client = client;
    this.serviceRegistration = serviceRegistration;
  }

  public Client getClient() {
    return this.client;
  }

  public String getWorkflowUri() {
    return getUri(this.workflowDomain);
  }

  public String getDomainUri(String domain) {
    return getUri(domain);
  }

  private String getUri(String domain) {
    this.uriCache.computeIfAbsent(domain, key -> {
      try {
        return this.serviceRegistration.get(key);
      } catch (RegistryException ex) {
        throw new ApplicationRuntimeException(ex);
      }
    });

    return this.uriCache.get(domain);
  }
}
