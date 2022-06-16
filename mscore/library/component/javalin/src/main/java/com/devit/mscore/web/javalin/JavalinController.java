package com.devit.mscore.web.javalin;

import static com.devit.mscore.util.AttributeConstants.CODE;
import static com.devit.mscore.util.AttributeConstants.ID;

import java.util.Arrays;

import com.devit.mscore.Service;
import com.devit.mscore.Synchronizer;
import com.devit.mscore.exception.ImplementationException;
import com.devit.mscore.exception.ValidationException;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.javalin.http.Handler;

/**
 * Implementation of controller for Javalin framework.
 * 
 * @author dkakunsi
 */
public class JavalinController {

    private static final Logger LOG = LoggerFactory.getLogger(JavalinController.class);

    protected static final String CONTENT_TYPE = "application/json";

    protected static final int SUCCESS = 200;

    protected Service service;

    protected Synchronizer synchronizer;

    public JavalinController(Service service) {
        this(service, service instanceof Synchronizer ? (Synchronizer) service : null);
    }

    protected JavalinController(Service service, Synchronizer synchronizer) {
        this.service = service;
        this.synchronizer = synchronizer;
    }

    public String getDomain() {
        return this.service.getDomain();
    }

    public Handler post() {
        return ctx -> {
            var applicationContext = JavalinApplicationContext.of(ctx);

            LOG.debug("BreadcrumbId: {}. Posting data.", applicationContext.getBreadcrumbId());
            var payload = ctx.body();
            var id = this.service.save(applicationContext, new JSONObject(payload));
            var data = new JSONObject();
            data.put(ID, id);

            ctx.status(SUCCESS).contentType(CONTENT_TYPE).result(data.toString());
        };
    }

    public Handler put() {
        return ctx -> {
            var applicationContext = JavalinApplicationContext.of(ctx);
            var key = ctx.pathParam(ID);
            LOG.debug("BreadcrumbId: {}. Putting data: {}.", applicationContext.getBreadcrumbId(), key);

            var payload = ctx.body();
            var data = new JSONObject(payload);
            data.put(ID, key);

            var id = this.service.save(applicationContext, data);
            data = new JSONObject();
            data.put(ID, id);

            ctx.status(SUCCESS).contentType(CONTENT_TYPE).result(data.toString());
        };
    }

    public Handler getOne() {
        return ctx -> {
            var applicationContext = JavalinApplicationContext.of(ctx);
            var key = ctx.pathParam(ID);

            LOG.debug("BreadcrumbId: {}. Get data with id: {}.", applicationContext.getBreadcrumbId(), key);
            var data = this.service.find(applicationContext, key);

            LOG.trace("Retrieved data for id: {}. {}", key, data);
            ctx.status(SUCCESS).contentType(CONTENT_TYPE).result(data.toString());
        };
    }

    public Handler getOneByCode() {
        return ctx -> {
            var applicationContext = JavalinApplicationContext.of(ctx);
            var code = ctx.pathParam(CODE);

            LOG.debug("BreadcrumbId: {}. Get data with code: {}.", applicationContext.getBreadcrumbId(), code);
            var data = this.service.findByCode(applicationContext, code);

            LOG.trace("Retrieved data for code: {}. {}", code, data);
            ctx.status(SUCCESS).contentType(CONTENT_TYPE).result(data.toString());
        };
    }

    public Handler getMany() {
        return ctx -> {
            var applicationContext = JavalinApplicationContext.of(ctx);

            var listOfId = ctx.queryParam("ids");
            if (listOfId == null) {
                throw new ValidationException("List of IDs is not provided");
            }

            LOG.debug("BreadcrumbId: {}. Get data with keys: {}.", applicationContext.getBreadcrumbId(), listOfId);
            var keys = Arrays.asList(listOfId.split(","));
            var data = this.service.find(applicationContext, keys);

            LOG.trace("Retrieved data for id: {}. {}", listOfId, data);
            ctx.status(SUCCESS).contentType(CONTENT_TYPE).result(data.toString());
        };
    }

    public Handler all() {
        return ctx -> {
            var applicationContext = JavalinApplicationContext.of(ctx);
            var data = this.service.all(applicationContext);

            LOG.debug("Retrieved all data. {}", data);
            ctx.status(SUCCESS).contentType(CONTENT_TYPE).result(data.toString());
        };
    }

    public Handler delete() {
        return ctx -> {
            throw new ImplementationException("Delete is not supported.");
        };
    }

    public Handler search() {
        return ctx -> {
            var applicationContext = JavalinApplicationContext.of(ctx);
            var criteria = ctx.body();
            var result = this.service.search(applicationContext, new JSONObject(criteria));

            ctx.status(SUCCESS).contentType(CONTENT_TYPE).result(result.toString());
        };
    }

    public Handler syncById() {
        return ctx -> {
            var applicationContext = JavalinApplicationContext.of(ctx);
            var key = ctx.pathParam(ID);

            LOG.debug("BreadcrumbId: {}. Sync data with id: {}.", applicationContext.getBreadcrumbId(), key);
            this.synchronizer.synchronize(applicationContext, key);

            var data = getSyncMessage();
            ctx.status(SUCCESS).contentType(CONTENT_TYPE).result(data.toString());
        };
    }

    public Handler syncAll() {
        return ctx -> {
            var applicationContext = JavalinApplicationContext.of(ctx);

            LOG.debug("BreadcrumbId: {}. Sync all.", applicationContext.getBreadcrumbId());

            this.synchronizer.synchronize(applicationContext);
            var data = getSyncMessage();
            ctx.status(SUCCESS).contentType(CONTENT_TYPE).result(data.toString());
        };
    }

    private JSONObject getSyncMessage() {
        var jsonResponse = new JSONObject();
        jsonResponse.put("message", "Synchronization process is in progress.");
        return jsonResponse;
    }
}
