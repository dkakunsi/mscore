package com.devit.mscore.workflow.api.javalin;

import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.post;
import static io.javalin.apibuilder.ApiBuilder.put;

import com.devit.mscore.web.javalin.JavalinEndpoint;

import java.util.List;

import io.javalin.apibuilder.EndpointGroup;

public class WorkflowEndpoint extends JavalinEndpoint {

  public WorkflowEndpoint(WorkflowController controller) {
    super(controller);
  }

  @Override
  public List<EndpointGroup> getEndpoints() {
    WorkflowController workflowController = (WorkflowController) this.controller;
    return List.of(() -> path(this.controller.getDomain(), () -> {
      post("definition/:" + WorkflowController.DEFINITION_ID, workflowController.createInstance());
      post("instance/:" + WorkflowController.ACTION, workflowController.createInstanceByAction());
      put("task/:" + WorkflowController.TASK_ID, workflowController.completeTask());
    }));
  }
}
