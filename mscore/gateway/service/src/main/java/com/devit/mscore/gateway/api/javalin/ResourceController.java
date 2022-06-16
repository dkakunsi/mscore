package com.devit.mscore.gateway.api.javalin;

import static com.devit.mscore.gateway.service.ServiceUtils.PAYLOAD;
import static com.devit.mscore.util.AttributeConstants.CODE;
import static com.devit.mscore.util.AttributeConstants.DOMAIN;
import static com.devit.mscore.util.AttributeConstants.ID;

import com.devit.mscore.exception.ValidationException;
import com.devit.mscore.gateway.service.ResourceService;
import com.devit.mscore.web.javalin.JavalinApplicationContext;
import com.devit.mscore.web.javalin.JavalinController;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import io.javalin.http.Handler;

public class ResourceController extends JavalinController {

    private ResourceService resourceService;

    public ResourceController(ResourceService resourceService) {
        super(resourceService);
        this.resourceService = resourceService;
    }

    @Override
    public Handler post() {
        return (ctx) -> {
            var applicationContext = JavalinApplicationContext.of(ctx);
            var domain = ctx.queryParam(DOMAIN);
            var payload = ctx.body();
            var response = this.resourceService.post(applicationContext, domain, new JSONObject(payload));

            ctx.status(response.getInt(CODE)).contentType(CONTENT_TYPE).result(response.get(PAYLOAD).toString());
        };
    }

    @Override
    public Handler put() {
        return (ctx) -> {
            var applicationContext = JavalinApplicationContext.of(ctx);
            var domain = ctx.queryParam(DOMAIN);
            var id = ctx.pathParam(ID);
            var payload = ctx.body();
            var response = this.resourceService.put(applicationContext, domain, id, new JSONObject(payload));

            ctx.status(response.getInt(CODE)).contentType(CONTENT_TYPE).result(response.get(PAYLOAD).toString());
        };
    }

    @Override
    public Handler getOne() {
        return (ctx) -> {
            var applicationContext = JavalinApplicationContext.of(ctx);
            var domain = ctx.queryParam(DOMAIN);
            var id = ctx.pathParam(ID);
            var response = this.resourceService.getById(applicationContext, domain, id);

            ctx.status(response.getInt(CODE)).contentType(CONTENT_TYPE).result(response.get(PAYLOAD).toString());
        };
    }

    @Override
    public Handler getOneByCode() {
        return (ctx) -> {
            var applicationContext = JavalinApplicationContext.of(ctx);
            var domain = ctx.queryParam(DOMAIN);
            var code = ctx.pathParam(CODE);
            var response = this.resourceService.getByCode(applicationContext, domain, code);

            ctx.status(response.getInt(CODE)).contentType(CONTENT_TYPE).result(response.get(PAYLOAD).toString());
        };
    }

    @Override
    public Handler getMany() {
        return (ctx) -> {
            var applicationContext = JavalinApplicationContext.of(ctx);
            var domain = ctx.queryParam(DOMAIN);
            var ids = ctx.queryParam("ids");
            var response = this.resourceService.getMany(applicationContext, domain, ids);

            ctx.status(response.getInt(CODE)).contentType(CONTENT_TYPE).result(response.get(PAYLOAD).toString());
        };
    }

    @Override
    public Handler syncById() {
        return (ctx) -> {
            var applicationContext = JavalinApplicationContext.of(ctx);
            var domain = ctx.queryParam(DOMAIN);
            var id = ctx.pathParam(ID);
            var response = this.resourceService.syncById(applicationContext, domain, id);

            ctx.status(response.getInt(CODE)).contentType(CONTENT_TYPE).result(response.get(PAYLOAD).toString());
        };
    }

    @Override
    public Handler syncAll() {
        return (ctx) -> {
            var applicationContext = JavalinApplicationContext.of(ctx);
            var domain = ctx.queryParam(DOMAIN);
            var response = this.resourceService.sync(applicationContext, domain);

            ctx.status(response.getInt(CODE)).contentType(CONTENT_TYPE).result(response.get(PAYLOAD).toString());
        };
    }

    @Override
    public Handler search() {
        return ctx -> {
            var applicationContext = JavalinApplicationContext.of(ctx);
            var criteria = ctx.body();
            if (StringUtils.isEmpty(criteria)) {
                throw new ValidationException("Search criteria is invalid.");
            }

            var json = new JSONObject(criteria);
            var domain = ctx.queryParam(DOMAIN);
            var response = ((ResourceService) this.service).search(applicationContext, domain, json);

            ctx.status(SUCCESS).contentType(CONTENT_TYPE).result(response.get(PAYLOAD).toString());
        };
    }
}
