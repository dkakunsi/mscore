package com.devit.mscore.gateway.api.javalin;

import static com.devit.mscore.util.AttributeConstants.ID;

import com.devit.mscore.Logger;
import com.devit.mscore.exception.ApplicationRuntimeException;
import com.devit.mscore.gateway.service.EventEmitter;
import com.devit.mscore.logging.ApplicationLogger;
import com.devit.mscore.web.javalin.JavalinController;

import org.json.JSONObject;

import io.javalin.http.Handler;

public class WorkflowEventController extends JavalinController {

  private static final Logger LOGGER = ApplicationLogger.getLogger(WorkflowEventController.class);

  private EventEmitter eventEmitter;

  public WorkflowEventController(EventEmitter eventEmitter) {
    super(eventEmitter);
    try {
      this.eventEmitter = (EventEmitter) eventEmitter.clone();
    } catch (CloneNotSupportedException ex) {
      throw new ApplicationRuntimeException(ex);
    }
  }

  @Override
  public String getDomain() {
    return "api/workflow/v2";
  }

  @Override
  public Handler put() {
    return ctx -> {
      var taskId = ctx.pathParam(ID);
      LOGGER.info("Receiving put request at {}", ctx.path());
      var taskResponse = new JSONObject(ctx.body());
      var resourceId = this.eventEmitter.complete(taskId, taskResponse);
      var result = new JSONObject().put(ID, resourceId);
      ctx.status(200).contentType(CONTENT_TYPE).result(result.toString());
    };
  }
}