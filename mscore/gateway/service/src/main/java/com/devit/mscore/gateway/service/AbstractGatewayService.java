package com.devit.mscore.gateway.service;

import com.devit.mscore.Service;
import com.devit.mscore.ServiceRegistration;
import com.devit.mscore.exception.ApplicationRuntimeException;
import com.devit.mscore.exception.RegistryException;
import com.devit.mscore.web.Client;

public abstract class AbstractGatewayService implements Service {

  protected Client client;

  protected ServiceRegistration serviceRegistration;

  protected AbstractGatewayService(ServiceRegistration serviceRegistration, Client client) {
    this.serviceRegistration = serviceRegistration;
    this.client = client;
  }

  protected String getUri(String domain) {
    try {
      return this.serviceRegistration.get(domain);
    } catch (RegistryException ex) {
      throw new ApplicationRuntimeException(ex);
    }
  }
}
