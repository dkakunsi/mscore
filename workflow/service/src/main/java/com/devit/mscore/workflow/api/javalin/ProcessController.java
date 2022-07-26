package com.devit.mscore.workflow.api.javalin;

import static com.devit.mscore.WorkflowConstants.INSTANCE_ID;
import static com.devit.mscore.util.Constants.PROCESS;
import static com.devit.mscore.util.Constants.UNKNOWN;

import com.devit.mscore.Event;
import com.devit.mscore.WorkflowService;
import com.devit.mscore.web.javalin.JavalinController;

import java.util.HashMap;

import org.json.JSONObject;

import io.javalin.http.Handler;

public class ProcessController extends JavalinController {

  public ProcessController(WorkflowService workflowProcess) {
    super(workflowProcess);
  }

  @Override
  public String getBasePath() {
    return PROCESS;
  }

  @Override
  public Handler post() {
    return ctx -> {
      var payload = new JSONObject(ctx.body());
      var event = Event.of(payload);
      var action = event.getAction();
      var entity = event.getData();
      var variables = event.getVariables() != null ? event.getVariables().toMap() : new HashMap<String, Object>();
      var processInstance = ((WorkflowService) service).executeWorkflow(action, entity, variables);
      var processInstanceId = processInstance.map(pi -> pi.getId()).orElse(UNKNOWN);

      var result = new JSONObject().put(INSTANCE_ID, processInstanceId);
      ctx.status(SUCCESS).contentType(CONTENT_TYPE).result(result.toString());
    };
  }
}
