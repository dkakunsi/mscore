package com.devit.mscore.workflow.api.javalin;

import static com.devit.mscore.util.Utils.VARIABLE;

import com.devit.mscore.Event;
import com.devit.mscore.Logger;
import com.devit.mscore.WorkflowService;
import com.devit.mscore.logging.ApplicationLogger;
import com.devit.mscore.web.javalin.JavalinController;

import org.json.JSONObject;

import io.javalin.http.Handler;

public class WorkflowController extends JavalinController {

  private static final Logger LOGGER = ApplicationLogger.getLogger(WorkflowController.class);

  // TODO: move to core library constant
  private static final String ACTION = "action";

  private static final String ENTITY = "entity";

  private WorkflowService workflowProcess;

  public WorkflowController(WorkflowService workflowProcess) {
    super(workflowProcess);
    this.workflowProcess = workflowProcess;
  }

  @Override
  public String getBasePath() {
    return "process";
  }

  @Override
  public Handler post() {
    return ctx -> {
      try {
        var payload = new JSONObject(ctx.body());
        var event = Event.of(payload);
        var entity = event.getData().getJSONObject(ENTITY);
        var variables = event.getData().optJSONObject(VARIABLE);
        // TODO: load from event
        var action = ctx.header(ACTION);
        var processInstance = this.workflowProcess.executeWorkflow(action, entity, variables.toMap());
  
        var result = new JSONObject().put("instanceId", processInstance.getId());
        ctx.status(SUCCESS).contentType(CONTENT_TYPE).result(result.toString());  
      } catch (Throwable ex) {
        LOGGER.error(ex.getMessage(), ex);
        throw ex;
      }
    };
  }
}
