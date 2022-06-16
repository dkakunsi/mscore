package com.devit.mscore.gateway.api.javalin;

import static com.devit.mscore.util.AttributeConstants.ID;

import com.devit.mscore.gateway.service.WorkflowService;
import com.devit.mscore.web.javalin.JavalinApplicationContext;
import com.devit.mscore.web.javalin.JavalinController;

import org.json.JSONObject;

import io.javalin.http.Handler;

public class WorkflowController extends JavalinController {

    private WorkflowService workflowService;

    public WorkflowController(WorkflowService workflowService) {
        super(workflowService);
        this.workflowService = workflowService;
    }

    @Override
    public Handler put() {
        return ctx -> {
            var applicationContext = JavalinApplicationContext.of(ctx);
            var taskId = ctx.pathParam(ID);
            var payload = ctx.body();
            this.workflowService.completeTask(applicationContext, taskId, new JSONObject(payload));

            ctx.status(SUCCESS);
        };
    }
}
