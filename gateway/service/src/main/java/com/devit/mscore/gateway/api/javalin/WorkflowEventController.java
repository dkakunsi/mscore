package com.devit.mscore.gateway.api.javalin;

import static com.devit.mscore.ApplicationContext.getContext;
import static com.devit.mscore.ApplicationContext.setContext;
import static com.devit.mscore.util.AttributeConstants.ID;
import static com.devit.mscore.util.Utils.EVENT_TYPE;

import com.devit.mscore.Event;
import com.devit.mscore.gateway.service.EventEmitter;
import com.devit.mscore.web.javalin.JavalinApplicationContext;
import com.devit.mscore.web.javalin.JavalinController;

import java.util.Map;

import org.json.JSONObject;

import io.javalin.http.Handler;

public class WorkflowEventController extends JavalinController {

  private EventEmitter eventEmitter;

  public WorkflowEventController(EventEmitter eventEmitter) {
    super(eventEmitter);
    this.eventEmitter = eventEmitter;
  }

  @Override
  public String getDomain() {
    return "api/v2/workflow";
  }

  @Override
  public Handler put() {
    return ctx -> {
      updateContext();

      var taskId = ctx.pathParam(ID);
      var taskResponse = new JSONObject(ctx.body());
      var resourceId = this.eventEmitter.updateTask(taskId, taskResponse);
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