package com.devit.mscore.gateway.service;

import com.devit.mscore.Resource;
import com.devit.mscore.Service;
import com.devit.mscore.ServiceRegistration;
import com.devit.mscore.exception.ApplicationRuntimeException;
import com.devit.mscore.exception.RegistryException;
import com.devit.mscore.web.Client;

public abstract class AbstractGatewayService implements Service {

  public static final String PAYLOAD = "payload";

  protected static final String WEBCLIENT_EXCEPTION_MESSAGE = "Cannot retrieve the url from registry";

  protected Client client;

  protected ServiceRegistration serviceRegistration;

  protected AbstractGatewayService(ServiceRegistration serviceRegistration, Client client) {
    this.serviceRegistration = serviceRegistration;
    this.client = client;
  }

  protected String getUri(String domain) {
    try {
      return serviceRegistration.get(domain);
    } catch (RegistryException ex) {
      throw new ApplicationRuntimeException(ex);
    }
  }

  @Override
  public Resource getSchema() {
    throw new UnsupportedOperationException();
  }
}
