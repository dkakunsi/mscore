package com.devit.mscore.gateway.api.javalin;

import static com.devit.mscore.ApplicationContext.setContext;
import static com.devit.mscore.util.AttributeConstants.ID;
import static com.devit.mscore.util.Utils.EVENT_TYPE;

import com.devit.mscore.Event;
import com.devit.mscore.Logger;
import com.devit.mscore.gateway.service.EventEmitter;
import com.devit.mscore.logging.ApplicationLogger;
import com.devit.mscore.web.javalin.JavalinApplicationContext;
import com.devit.mscore.web.javalin.JavalinController;

import java.util.HashMap;

import org.json.JSONObject;

import io.javalin.http.Handler;

public class WorkflowEventController extends JavalinController {

  private static final Logger LOGGER = ApplicationLogger.getLogger(WorkflowEventController.class);

  private EventEmitter eventEmitter;

  public WorkflowEventController(EventEmitter eventEmitter) {
    super(eventEmitter);
    this.eventEmitter = eventEmitter;
  }

  @Override
  public String getDomain() {
    return "api/workflow/v2";
  }

  @Override
  public Handler put() {
    return ctx -> {
      var contextData = new HashMap<String, Object>();
      contextData.put(EVENT_TYPE, Event.Type.TASK);
      var context = JavalinApplicationContext.of(ctx);
      setContext(context);
      
      var taskId = ctx.pathParam(ID);
      LOGGER.info("Receiving put request at {}", ctx.path());
      var taskResponse = new JSONObject(ctx.body());
      var resourceId = this.eventEmitter.updateTask(taskId, taskResponse);
      var result = new JSONObject().put(ID, resourceId);
      ctx.status(200).contentType(CONTENT_TYPE).result(result.toString());
    };
  }
}