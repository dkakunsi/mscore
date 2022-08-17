package com.devit.mscore.web.javalin;

import static com.devit.mscore.util.AttributeConstants.CODE;
import static com.devit.mscore.util.AttributeConstants.ID;

import java.util.Arrays;

import com.devit.mscore.Logger;
import com.devit.mscore.Service;
import com.devit.mscore.Synchronizer;
import com.devit.mscore.exception.ImplementationException;
import com.devit.mscore.exception.ValidationException;
import com.devit.mscore.logging.ApplicationLogger;

import org.json.JSONObject;

import io.javalin.http.Handler;

/**
 * Implementation of controller for Javalin framework.
 * 
 * @author dkakunsi
 */
public class JavalinController {

  private static final Logger LOG = ApplicationLogger.getLogger(JavalinController.class);

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
      LOG.debug("Posting data.");
      var payload = ctx.body();
      var id = this.service.save(new JSONObject(payload));
      var data = new JSONObject();
      data.put(ID, id);

      ctx.status(SUCCESS).contentType(CONTENT_TYPE).result(data.toString());
    };
  }

  public Handler put() {
    return ctx -> {
      var key = ctx.pathParam(ID);
      LOG.debug("Putting data: {}.", key);

      var payload = ctx.body();
      var data = new JSONObject(payload);
      data.put(ID, key);

      var id = this.service.save(data);
      data = new JSONObject();
      data.put(ID, id);

      ctx.status(SUCCESS).contentType(CONTENT_TYPE).result(data.toString());
    };
  }

  public Handler getOne() {
    return ctx -> {
      var key = ctx.pathParam(ID);

      LOG.debug("Get data with id: {}.", key);
      var data = this.service.find(key);

      LOG.trace("Retrieved data for id: {}. {}", key, data);
      ctx.status(SUCCESS).contentType(CONTENT_TYPE).result(data.toString());
    };
  }

  public Handler getOneByCode() {
    return ctx -> {
      var code = ctx.pathParam(CODE);

      LOG.debug("Get data with code: {}.", code);
      var data = this.service.findByCode(code);

      LOG.trace("Retrieved data for code: {}. {}", code, data);
      ctx.status(SUCCESS).contentType(CONTENT_TYPE).result(data.toString());
    };
  }

  public Handler getMany() {
    return ctx -> {
      var listOfId = ctx.queryParam("ids");
      if (listOfId == null) {
        throw new ValidationException("List of IDs is not provided");
      }

      LOG.debug("Get data with keys: {}.", listOfId);
      var keys = Arrays.asList(listOfId.split(","));
      var data = this.service.find(keys);

      LOG.trace("Retrieved data for id: {}. {}", listOfId, data);
      ctx.status(SUCCESS).contentType(CONTENT_TYPE).result(data.toString());
    };
  }

  public Handler all() {
    return ctx -> {
      var data = this.service.all();

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
      var criteria = ctx.body();
      var result = this.service.search(new JSONObject(criteria));

      ctx.status(SUCCESS).contentType(CONTENT_TYPE).result(result.toString());
    };
  }

  public Handler syncById() {
    return ctx -> {
      var key = ctx.pathParam(ID);

      LOG.debug("Sync data with id: {}.", key);
      this.synchronizer.synchronize(key);

      var data = getSyncMessage();
      ctx.status(SUCCESS).contentType(CONTENT_TYPE).result(data.toString());
    };
  }

  public Handler syncAll() {
    return ctx -> {
      LOG.debug("Sync all.");

      this.synchronizer.synchronize();
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
