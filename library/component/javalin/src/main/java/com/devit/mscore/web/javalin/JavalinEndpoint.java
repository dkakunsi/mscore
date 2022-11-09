package com.devit.mscore.web.javalin;

import static com.devit.mscore.util.Constants.CODE;
import static com.devit.mscore.util.Constants.ID;
import static io.javalin.apibuilder.ApiBuilder.delete;
import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.post;
import static io.javalin.apibuilder.ApiBuilder.put;

import com.devit.mscore.Service;
import com.devit.mscore.web.Endpoint;

import java.util.ArrayList;
import java.util.List;

import io.javalin.apibuilder.EndpointGroup;

public class JavalinEndpoint implements Endpoint {

  protected JavalinController controller;

  public JavalinEndpoint(Service service) {
    this(new JavalinController(service));
  }

  public JavalinEndpoint(JavalinController controller) {
    this.controller = controller;
  }

  @Override
  public void register() {
    getEndpoints().stream().forEach(EndpointGroup::addEndpoints);
  }

  public List<EndpointGroup> getEndpoints() {
    var endpoints = new ArrayList<EndpointGroup>();
    endpoints.add(() -> path(controller.getBasePath(), () -> {
      post(controller.post());
      post("search", controller.search());
      post("sync", controller.syncAll());
      get(controller.all());
      get("code/:" + CODE, controller.getOneByCode());
      get("keys", controller.getMany());
      path(":" + ID, () -> {
        get(controller.getOne());
        put(controller.put());
        delete(controller.delete());
        post("sync", controller.syncById());
      });
    }));

    return endpoints;
  }
}
