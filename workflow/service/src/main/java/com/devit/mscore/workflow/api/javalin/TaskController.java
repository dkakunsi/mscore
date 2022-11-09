package com.devit.mscore.workflow.api.javalin;

import static com.devit.mscore.util.AttributeConstants.getId;

import com.devit.mscore.Event;
import com.devit.mscore.WorkflowService;
import com.devit.mscore.web.javalin.JavalinController;

import java.util.HashMap;

import org.json.JSONObject;

import io.javalin.http.Handler;

public class TaskController extends JavalinController {

  public TaskController(WorkflowService workflowService) {
    super(workflowService);
  }

  @Override
  public String getBasePath() {
    return "task";
  }

  @Override
  public Handler post() {
    return ctx -> {
      var payload = new JSONObject(ctx.body());
      var event = Event.of(payload);
      var taskId = getId(event.getData());
      var variables = event.getVariables() != null ? event.getVariables().toMap() : new HashMap<String, Object>();
      ((WorkflowService) service).completeTask(taskId, variables);

      ctx.status(SUCCESS);
    };
  }
}
