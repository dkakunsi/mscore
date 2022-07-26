package com.devit.mscore.gateway.api;

import com.devit.mscore.AuthenticationProvider;
import com.devit.mscore.Configuration;
import com.devit.mscore.gateway.api.javalin.ResourceController;
import com.devit.mscore.gateway.api.javalin.TaskController;
import com.devit.mscore.gateway.service.ResourceService;
import com.devit.mscore.gateway.service.TaskService;
import com.devit.mscore.web.javalin.JavalinApiFactory;

public class ApiFactory extends JavalinApiFactory {

  private ApiFactory(Configuration configuration) {
    super(configuration);
  }

  public static ApiFactory of(Configuration configuration, AuthenticationProvider authenticationProvider) {
    var manager = new ApiFactory(configuration);
    manager.authenticationProvider = authenticationProvider;
    return manager;
  }

  public ApiFactory add(ResourceService resourceService) {
    add(new ResourceController(resourceService));
    return this;
  }

  public ApiFactory add(TaskService taskService) {
    add(new TaskController(taskService));
    return this;
  }
}
