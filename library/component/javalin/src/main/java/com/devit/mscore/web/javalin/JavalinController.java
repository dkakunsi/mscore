package com.devit.mscore.web.javalin;

import static com.devit.mscore.util.Constants.CODE;
import static com.devit.mscore.util.Constants.ID;

import com.devit.mscore.Service;
import com.devit.mscore.Synchronizer;
import com.devit.mscore.exception.ImplementationException;
import com.devit.mscore.exception.ValidationException;

import java.util.Arrays;

import org.json.JSONObject;

import io.javalin.http.Handler;

/**
 * Implementation of controller for Javalin framework.
 *
 * @author dkakunsi
 */
public class JavalinController {

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

  @Deprecated
  public String getDomain() {
    return service.getDomain();
  }

  public String getBasePath() {
    return getDomain();
  }

  public Handler post() {
    return ctx -> {
      var payload = ctx.body();
      var id = service.save(new JSONObject(payload));
      var data = new JSONObject().put(ID, id);

      ctx.status(SUCCESS).contentType(CONTENT_TYPE).result(data.toString());
    };
  }

  public Handler put() {
    return ctx -> {
      var key = ctx.pathParam(ID);
      var payload = ctx.body();
      var data = new JSONObject(payload).put(ID, key);

      var id = service.save(data);
      data = new JSONObject().put(ID, id);

      ctx.status(SUCCESS).contentType(CONTENT_TYPE).result(data.toString());
    };
  }

  public Handler getOne() {
    return ctx -> {
      var key = ctx.pathParam(ID);
      var data = service.find(key);
      ctx.status(SUCCESS).contentType(CONTENT_TYPE).result(data.toString());
    };
  }

  public Handler getOneByCode() {
    return ctx -> {
      var code = ctx.pathParam(CODE);
      var data = service.findByCode(code);
      ctx.status(SUCCESS).contentType(CONTENT_TYPE).result(data.toString());
    };
  }

  public Handler getMany() {
    return ctx -> {
      var listId = ctx.queryParam("ids");
      if (listId == null) {
        throw new ValidationException("List of IDs is not provided");
      }

      var keys = Arrays.asList(listId.split(","));
      var data = service.find(keys);
      ctx.status(SUCCESS).contentType(CONTENT_TYPE).result(data.toString());
    };
  }

  public Handler all() {
    return ctx -> {
      var data = service.all();
      ctx.status(SUCCESS).contentType(CONTENT_TYPE).result(data.toString());
    };
  }

  public Handler delete() {
    return ctx -> {
      throw new ImplementationException("Delete is not supported");
    };
  }

  public Handler search() {
    return ctx -> {
      var criteria = ctx.body();
      var result = service.search(new JSONObject(criteria));
      ctx.status(SUCCESS).contentType(CONTENT_TYPE).result(result.toString());
    };
  }

  public Handler syncById() {
    return ctx -> {
      var key = ctx.pathParam(ID);
      synchronizer.synchronize(key);
      var data = getSyncMessage();
      ctx.status(SUCCESS).contentType(CONTENT_TYPE).result(data.toString());
    };
  }

  public Handler syncAll() {
    return ctx -> {
      synchronizer.synchronize();
      var data = getSyncMessage();
      ctx.status(SUCCESS).contentType(CONTENT_TYPE).result(data.toString());
    };
  }

  private JSONObject getSyncMessage() {
    return new JSONObject().put("message", "Synchronization process is in progress");
  }
}
