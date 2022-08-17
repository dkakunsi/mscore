package com.devit.mscore.workflow.api.javalin;

import static com.devit.mscore.util.Utils.VARIABLE;

import java.util.HashMap;
import java.util.Optional;

import com.devit.mscore.Service;
import com.devit.mscore.exception.ApplicationException;
import com.devit.mscore.web.javalin.JavalinController;
import com.devit.mscore.WorkflowProcess;

import org.json.JSONObject;

import io.javalin.http.Handler;

public class WorkflowController extends JavalinController {

  static final String ACTION = "action";

  static final String DEFINITION_ID = "processDefinitionId";

  static final String TASK_ID = "taskId";

  private static final String NO_WORKFLOW = "No workflow process is registered.";

  private Optional<WorkflowProcess> workflowProcess;

  public WorkflowController(Service service) {
    super(service);
    if (service instanceof WorkflowProcess) {
      this.workflowProcess = Optional.of((WorkflowProcess) service);
    } else {
      this.workflowProcess = Optional.empty();
    }
  }

  public Handler createInstance() {
    return ctx -> {
      if (this.workflowProcess.isEmpty()) {
        throw new ApplicationException(NO_WORKFLOW);
      }

      var processDefinitionId = ctx.pathParam(DEFINITION_ID);
      var entity = new JSONObject(ctx.body());
      var variables = ctx.header(VARIABLE) != null ? new JSONObject(ctx.header(VARIABLE)).toMap()
          : new HashMap<String, Object>();
      var processInstance = this.workflowProcess.get().createInstance(processDefinitionId, entity, variables);

      ctx.status(SUCCESS).contentType(CONTENT_TYPE)
          .result(new JSONObject().put("instanceId", processInstance.getId()).toString());
    };
  }

  public Handler createInstanceByAction() {
    return ctx -> {
      if (this.workflowProcess.isEmpty()) {
        throw new ApplicationException(NO_WORKFLOW);
      }

      var action = ctx.pathParam(ACTION);
      var entity = new JSONObject(ctx.body());
      var variables = ctx.header(VARIABLE) != null ? new JSONObject(ctx.header(VARIABLE)).toMap()
          : new HashMap<String, Object>();
      var processInstance = this.workflowProcess.get().createInstanceByAction(action, entity, variables);

      ctx.status(SUCCESS).contentType(CONTENT_TYPE)
          .result(new JSONObject().put("instanceId", processInstance.getId()).toString());
    };
  }

  public Handler completeTask() {
    return ctx -> {
      if (this.workflowProcess.isEmpty()) {
        throw new ApplicationException(NO_WORKFLOW);
      }

      var taskId = ctx.pathParam(TASK_ID);
      var taskResponse = ctx.body();
      this.workflowProcess.get().completeTask(taskId, new JSONObject(taskResponse));

      ctx.status(SUCCESS);
    };
  }
}
