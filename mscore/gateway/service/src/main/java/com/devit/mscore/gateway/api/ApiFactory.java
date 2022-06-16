package com.devit.mscore.gateway.api;

import com.devit.mscore.ApplicationContext;
import com.devit.mscore.AuthenticationProvider;
import com.devit.mscore.Configuration;
import com.devit.mscore.gateway.service.ResourceService;
import com.devit.mscore.gateway.service.WorkflowService;
import com.devit.mscore.gateway.api.javalin.ResourceController;
import com.devit.mscore.gateway.api.javalin.WorkflowController;
import com.devit.mscore.web.javalin.JavalinApiFactory;

public class ApiFactory extends JavalinApiFactory {

    private ApiFactory(ApplicationContext context, Configuration configuration) {
        super(context, configuration);
    }

    public static ApiFactory of(ApplicationContext context, Configuration configuration, AuthenticationProvider authentication) {
        var manager = new ApiFactory(context, configuration);
        manager.authentication = authentication;
        return manager;
    }

    public ApiFactory add(ResourceService resourceService) {
        add(new ResourceController(resourceService));
        return this;
    }

    public ApiFactory add(WorkflowService workflowService) {
        add(new WorkflowController(workflowService));
        return this;
    }
}
