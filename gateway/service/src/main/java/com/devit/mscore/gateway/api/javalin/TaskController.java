package com.devit.mscore.gateway.api.javalin;

import static com.devit.mscore.ApplicationContext.getContext;
import static com.devit.mscore.ApplicationContext.setContext;
import static com.devit.mscore.util.Constants.EVENT_TYPE;
import static com.devit.mscore.util.Constants.ID;

import com.devit.mscore.Event;
import com.devit.mscore.gateway.service.TaskService;
import com.devit.mscore.web.javalin.JavalinApplicationContext;
import com.devit.mscore.web.javalin.JavalinController;

import java.util.Map;

import org.json.JSONObject;

import io.javalin.http.Handler;

public class TaskController extends JavalinController {

  public TaskController(TaskService taskService) {
    super(taskService);
  }

  @Override
  public String getBasePath() {
    return "api/v2/task";
  }

  @Override
  public Handler put() {
    return ctx -> {
      updateContext();

      var taskId = ctx.pathParam(ID);
      var taskResponse = new JSONObject(ctx.body());
      var resourceId = ((TaskService) service).completeTask(taskId, taskResponse);
      var result = new JSONObject().put(ID, resourceId);
      ctx.status(200).contentType(CONTENT_TYPE).result(result.toString());
    };
  }

  private void updateContext() {
    var contextData = Map.of(EVENT_TYPE, (Object) Event.Type.TASK);
    var context = JavalinApplicationContext.of(getContext(), contextData);
    setContext(context);
  }
}