package com.devit.mscore.gateway.api.javalin;

import static com.devit.mscore.util.AttributeConstants.ID;

import com.devit.mscore.gateway.service.EventEmitter;
import com.devit.mscore.web.javalin.JavalinController;

import org.json.JSONObject;

import io.javalin.http.Handler;

public class ResourceEventController extends JavalinController {

  private EventEmitter eventEmitter;

  public ResourceEventController(EventEmitter eventEmitter) {
    super(eventEmitter);
    this.eventEmitter = eventEmitter;
  }

  @Override
  public String getDomain() {
    return "api/v2/resource";
  }

  @Override
  public Handler post() {
    return ctx -> {
      var payload = ctx.body();
      var resourceId = this.eventEmitter.create(new JSONObject(payload));
      var result = new JSONObject();
      result.put(ID, resourceId);
      ctx.status(200).contentType(CONTENT_TYPE).result(result.toString());
    };
  }

  @Override
  public Handler put() {
    return ctx -> {
      var id = ctx.pathParam(ID);
      var payload = ctx.body();
      var data = new JSONObject(payload).put(ID, id);

      var resourceId = this.eventEmitter.update(data);
      var result = new JSONObject().put(ID, resourceId);
      ctx.status(200).contentType(CONTENT_TYPE).result(result.toString());
    };
  }

  @Override
  public Handler delete() {
    return ctx -> {
      var id = ctx.pathParam(ID);
      var payload = ctx.body();
      var data = new JSONObject(payload).put(ID, id);

      var resourceId = this.eventEmitter.remove(data);
      var result = new JSONObject().put(ID, resourceId);
      ctx.status(200).contentType(CONTENT_TYPE).result(result.toString());
    };
  }
}
