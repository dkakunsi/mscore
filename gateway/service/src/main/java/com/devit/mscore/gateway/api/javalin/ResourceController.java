package com.devit.mscore.gateway.api.javalin;

import static com.devit.mscore.gateway.service.AbstractGatewayService.PAYLOAD;
import static com.devit.mscore.util.AttributeConstants.CODE;
import static com.devit.mscore.util.AttributeConstants.DOMAIN;
import static com.devit.mscore.util.AttributeConstants.ID;

import com.devit.mscore.exception.ValidationException;
import com.devit.mscore.gateway.service.ResourceService;
import com.devit.mscore.web.javalin.JavalinController;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import io.javalin.http.Handler;

public class ResourceController extends JavalinController {

  public ResourceController(ResourceService resourceService) {
    super(resourceService);
  }

  @Override
  public String getBasePath() {
    return "api/v2/resource";
  }

  @Override
  public Handler post() {
    return ctx -> {
      var domain = ctx.queryParam(DOMAIN);
      var payload = new JSONObject(ctx.body());
      var response = ((ResourceService) service).create(domain, payload);

      var responseMessage = new JSONObject();
      responseMessage.put(ID, response);

      ctx.status(SUCCESS).contentType(CONTENT_TYPE).result(responseMessage.toString());
    };
  }

  @Override
  public Handler put() {
    return ctx -> {
      var domain = ctx.queryParam(DOMAIN);
      var id = ctx.pathParam(ID);
      var payload = new JSONObject(ctx.body());
      var response = ((ResourceService) service).update(domain, id, payload);

      var responseMessage = new JSONObject();
      responseMessage.put(ID, response);

      ctx.status(SUCCESS).contentType(CONTENT_TYPE).result(responseMessage.toString());
    };
  }

  @Override
  public Handler getOne() {
    return ctx -> {
      var domain = ctx.queryParam(DOMAIN);
      var id = ctx.pathParam(ID);
      var response = ((ResourceService) service).getById(domain, id);

      ctx.status(response.getInt(CODE)).contentType(CONTENT_TYPE).result(response.get(PAYLOAD).toString());
    };
  }

  @Override
  public Handler getOneByCode() {
    return ctx -> {
      var domain = ctx.queryParam(DOMAIN);
      var code = ctx.pathParam(CODE);
      var response = ((ResourceService) service).getByCode(domain, code);

      ctx.status(response.getInt(CODE)).contentType(CONTENT_TYPE).result(response.get(PAYLOAD).toString());
    };
  }

  @Override
  public Handler getMany() {
    return ctx -> {
      var domain = ctx.queryParam(DOMAIN);
      var ids = ctx.queryParam("ids");
      var response = ((ResourceService) service).getMany(domain, ids);

      ctx.status(response.getInt(CODE)).contentType(CONTENT_TYPE).result(response.get(PAYLOAD).toString());
    };
  }

  @Override
  public Handler syncById() {
    return ctx -> {
      var domain = ctx.queryParam(DOMAIN);
      var id = ctx.pathParam(ID);
      var response = ((ResourceService) service).syncById(domain, id);

      ctx.status(response.getInt(CODE)).contentType(CONTENT_TYPE).result(response.get(PAYLOAD).toString());
    };
  }

  @Override
  public Handler syncAll() {
    return ctx -> {
      var domain = ctx.queryParam(DOMAIN);
      var response = ((ResourceService) service).sync(domain);

      ctx.status(response.getInt(CODE)).contentType(CONTENT_TYPE).result(response.get(PAYLOAD).toString());
    };
  }

  @Override
  public Handler search() {
    return ctx -> {
      var criteria = ctx.body();
      if (StringUtils.isEmpty(criteria)) {
        throw new ValidationException("Search criteria is invalid");
      }

      var json = new JSONObject(criteria);
      var domain = ctx.queryParam(DOMAIN);
      var response = ((ResourceService) service).search(domain, json);

      ctx.status(SUCCESS).contentType(CONTENT_TYPE).result(response.get(PAYLOAD).toString());
    };
  }
}
