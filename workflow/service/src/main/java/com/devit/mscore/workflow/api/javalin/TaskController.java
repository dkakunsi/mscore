package com.devit.mscore.workflow.api.javalin;

import static com.devit.mscore.util.AttributeConstants.ID;

import com.devit.mscore.Event;
import com.devit.mscore.WorkflowService;
import com.devit.mscore.web.javalin.JavalinController;

import org.json.JSONObject;

import io.javalin.http.Handler;

public class TaskController extends JavalinController{

  private WorkflowService workflowProcess;

  public TaskController(WorkflowService workflowProcess) {
    super(workflowProcess);
    this.workflowProcess = workflowProcess;
  }

  @Override
  public String getBasePath() {
    return "task";
  }

  @Override
  public Handler put() {
    return ctx -> {
      var taskId = ctx.pathParam(ID);
      var payload = new JSONObject(ctx.body());
      var event = Event.of(payload);
      var responseVariable = event.getData().toMap();
      workflowProcess.completeTask(taskId, responseVariable);

      ctx.status(SUCCESS);
    };
  }
}
