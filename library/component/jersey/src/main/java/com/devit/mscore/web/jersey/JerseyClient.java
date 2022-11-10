package com.devit.mscore.web.jersey;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.json.JSONObject;

import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation.Builder;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

public class JerseyClient extends Requester implements com.devit.mscore.web.Client {

  private Client client;

  JerseyClient(Client client) {
    this.client = client;
  }

  @Override
  public JSONObject delete(String uri) {
    var response = request(HttpMethod.DELETE, uri, new HashMap<>()).delete();
    return buildResponse(uri, response);
  }

  @Override
  public JSONObject get(String uri, Map<String, String> params) {
    var response = request(HttpMethod.GET, uri, params).get();
    return buildResponse(uri, response);
  }

  @Override
  public JSONObject post(String uri, Optional<JSONObject> payload) {
    Response response;
    if (payload.isPresent()) {
      response = request(HttpMethod.POST, uri, new HashMap<>()).post(Entity.json(payload.get().toString()));
    } else {
      response = request(HttpMethod.POST, uri, new HashMap<>()).post(null);
    }
    return buildResponse(uri, response);
  }

  @Override
  public JSONObject put(String uri, Optional<JSONObject> payload) {
    Response response;
    if (payload.isPresent()) {
      response = request(HttpMethod.PUT, uri, new HashMap<>()).put(Entity.json(payload.get().toString()));
    } else {
      response = request(HttpMethod.PUT, uri, new HashMap<>()).put(null);
    }
    return buildResponse(uri, response);
  }

  private Builder request(String method, String uri, Map<String, String> params) {

    var target = client.target(uri);
    if (params != null) {
      params.forEach(target::queryParam);
    }

    var builtHeaders = buildRequestHeader();
    var builder = target.request();
    builtHeaders.forEach(builder::header);
    builder.accept(MediaType.APPLICATION_JSON);

    logger.info(LOG_INFO_FORMAT, method, uri, getPrintableHeaders(builtHeaders));
    return builder;
  }
}
