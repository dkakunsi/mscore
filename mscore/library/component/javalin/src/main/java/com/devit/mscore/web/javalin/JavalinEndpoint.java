package com.devit.mscore.web.javalin;

import static com.devit.mscore.util.AttributeConstants.CODE;
import static com.devit.mscore.util.AttributeConstants.ID;
import static io.javalin.apibuilder.ApiBuilder.delete;
import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.post;
import static io.javalin.apibuilder.ApiBuilder.put;

import java.util.ArrayList;
import java.util.List;

import com.devit.mscore.Service;
import com.devit.mscore.web.Endpoint;

import io.javalin.apibuilder.EndpointGroup;

public class JavalinEndpoint implements Endpoint {

    protected JavalinController controller;

    public JavalinEndpoint(Service service) {
        this.controller = new JavalinController(service);
    }

    public JavalinEndpoint(JavalinController controller) {
        setController(controller);
    }

    public void setController(JavalinController controller) {
        this.controller = controller;
    }

    @Override
    public void register() {
        getEndpoints().stream().forEach(EndpointGroup::addEndpoints);
    }

    public List<EndpointGroup> getEndpoints() {
        var endpoints = new ArrayList<EndpointGroup>();
        endpoints.add(() -> path(this.controller.getDomain(), () -> {
                post(this.controller.post());
                post("search", this.controller.search());
                post("sync", this.controller.syncAll());
                get(this.controller.all());
                get("code/:" + CODE, this.controller.getOneByCode());
                get("keys", this.controller.getMany());
                path(":" + ID, () -> {
                    get(this.controller.getOne());
                    put(this.controller.put());
                    delete(this.controller.delete());
                    post("sync", this.controller.syncById());
                });
            })
        );

        return endpoints;
    }
}
