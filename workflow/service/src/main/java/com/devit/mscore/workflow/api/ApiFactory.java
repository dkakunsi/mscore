package com.devit.mscore.workflow.api;

import com.devit.mscore.AuthenticationProvider;
import com.devit.mscore.Configuration;
import com.devit.mscore.WorkflowService;
import com.devit.mscore.web.javalin.JavalinApiFactory;
import com.devit.mscore.web.javalin.JavalinEndpoint;
import com.devit.mscore.workflow.api.javalin.WorkflowController;
import com.devit.mscore.workflow.api.javalin.WorkflowTaskController;

public class ApiFactory extends JavalinApiFactory {

  private ApiFactory(Configuration configuration, AuthenticationProvider authenticationProvider) {
    super(configuration);
    this.authenticationProvider = authenticationProvider;
  }

  public static ApiFactory of(Configuration configuration, AuthenticationProvider authenticationProvider) {
    return new ApiFactory(configuration, authenticationProvider);
  }

  public ApiFactory addService(WorkflowService workflowService) {
    add(new JavalinEndpoint(new WorkflowController(workflowService)));
    add(new JavalinEndpoint(new WorkflowTaskController(workflowService)));
    return this;
  }
}
