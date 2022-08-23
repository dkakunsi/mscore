package com.devit.mscore.workflow.api;

import com.devit.mscore.AuthenticationProvider;
import com.devit.mscore.Configuration;
import com.devit.mscore.Service;
import com.devit.mscore.web.javalin.JavalinApiFactory;
import com.devit.mscore.workflow.api.javalin.WorkflowController;
import com.devit.mscore.workflow.api.javalin.WorkflowEndpoint;

public class ApiFactory extends JavalinApiFactory {

  private ApiFactory(Configuration configuration) {
    super(configuration);
  }

  public static ApiFactory of(Configuration configuration, AuthenticationProvider authenticationProvider) {
    var factory = new ApiFactory(configuration);
    factory.authenticationProvider = authenticationProvider;
    return factory;
  }

  public ApiFactory addService(Service service) {
    add(new WorkflowEndpoint(new WorkflowController(service)));
    return this;
  }
}
