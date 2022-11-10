package com.devit.mscore.gateway.api.javalin;

import static com.devit.mscore.ApplicationContext.getContext;
import static com.devit.mscore.ApplicationContext.setContext;
import static com.devit.mscore.util.Constants.EVENT_TYPE;
import static com.devit.mscore.util.Constants.ID;

import com.devit.mscore.Event;
import com.devit.mscore.gateway.service.TaskService;
import com.devit.mscore.web.javalin.JavalinApplicationContext;
import com.devit.mscore.web.javalin.JavalinController;

import java.util.HashMap;

import org.json.JSONObject;

import io.javalin.http.Handler;

public class TaskController extends JavalinController {

  private static final String RESPONSE = "response";

  public TaskController(TaskService taskService) {
    super(taskService);
  }

  @Override
  public String getBasePath() {
    return "api/v2/task";
  }

  @Override
  public Handler post() {
    return ctx -> {
      updateContext();

      var payload = new JSONObject(ctx.body());
      var taskId = payload.getString(ID);
      var response = payload.getJSONObject(RESPONSE);
      var resourceId = ((TaskService) service).completeTask(taskId, response);
      var result = new JSONObject().put(ID, resourceId);
      ctx.status(200).contentType(CONTENT_TYPE).result(result.toString());
    };
  }

  private void updateContext() {
    var contextData = new HashMap<String, Object>();
    contextData.put(EVENT_TYPE, Event.Type.TASK);
    var context = JavalinApplicationContext.of(getContext(), contextData);
    setContext(context);
  }
}