package com.devit.mscore.workflow.api.javalin;

import static com.devit.mscore.util.Constants.ACTION;
import static com.devit.mscore.util.Constants.ENTITY;
import static com.devit.mscore.util.Constants.VARIABLE;

import com.devit.mscore.Event;
import com.devit.mscore.WorkflowService;
import com.devit.mscore.web.javalin.JavalinController;

import org.json.JSONObject;

import io.javalin.http.Handler;

public class ProcessController extends JavalinController {

  private WorkflowService workflowProcess;

  public ProcessController(WorkflowService workflowProcess) {
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
      var payload = new JSONObject(ctx.body());
      var event = Event.of(payload);
      var entity = event.getData().getJSONObject(ENTITY);
      var variables = event.getData().optJSONObject(VARIABLE);
      var action = ctx.header(ACTION);
      var processInstance = this.workflowProcess.executeWorkflow(action, entity, variables.toMap());

      var result = new JSONObject().put("instanceId", processInstance.getId());
      ctx.status(SUCCESS).contentType(CONTENT_TYPE).result(result.toString());  
    };
  }
}
