package com.devit.mscore.workflow.api.javalin;

import static com.devit.mscore.util.AttributeConstants.ID;

import com.devit.mscore.Event;
import com.devit.mscore.Logger;
import com.devit.mscore.WorkflowService;
import com.devit.mscore.logging.ApplicationLogger;
import com.devit.mscore.web.javalin.JavalinController;

import org.json.JSONObject;

import io.javalin.http.Handler;

public class WorkflowTaskController extends JavalinController{

  private static final Logger LOGGER = ApplicationLogger.getLogger(WorkflowTaskController.class);

  private WorkflowService workflowProcess;

  public WorkflowTaskController(WorkflowService workflowProcess) {
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
      try {
        var taskId = ctx.pathParam(ID);
        var payload = new JSONObject(ctx.body());
        var event = Event.of(payload);
        var responseVariable = event.getData().toMap();
        workflowProcess.completeTask(taskId, responseVariable);
  
        ctx.status(SUCCESS);
      } catch (Throwable ex) {
        LOGGER.error(ex.getMessage(), ex);
        throw ex;
      }
    };
  }
}
