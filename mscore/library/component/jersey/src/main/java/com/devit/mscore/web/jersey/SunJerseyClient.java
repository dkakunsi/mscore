package com.devit.mscore.web.jersey;

import static com.devit.mscore.web.jersey.ResponseUtils.buildResponse;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import com.devit.mscore.Logger;
import com.devit.mscore.logging.ApplicationLogger;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.json.JSONObject;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource.Builder;

public class SunJerseyClient implements com.devit.mscore.web.Client, Requester {

  private static final Logger LOG = new ApplicationLogger(SunJerseyClient.class);

  private Client client;

  SunJerseyClient() {
    this(Client.create());
  }

  SunJerseyClient(Client client) {
    this.client = client;
  }

  @Override
  public com.devit.mscore.web.Client createNew() {
    return new SunJerseyClient();
  }

  @Override
  public JSONObject delete(String uri) {
    LOG.debug("Sending DELETE '{}'", uri);
    var response = request(uri, new HashMap<>(), new HashMap<>()).delete(ClientResponse.class);
    return buildResponse(uri, response);
  }

  @Override
  public JSONObject get(String uri, Map<String, String> params) {
    LOG.debug("Sending GET '{}' with params: {}", uri, params);
    var response = request(uri, params, new HashMap<>()).get(ClientResponse.class);
    return buildResponse(uri, response);
  }

  @Override
  public JSONObject post(String uri, Optional<JSONObject> payload) {
    LOG.debug("Sending POST '{}': {}", uri, payload);
    ClientResponse response;
    if (payload.isPresent()) {
      response = request(uri, new HashMap<>(), new HashMap<>()).post(ClientResponse.class,
          payload.get().toString());
    } else {
      response = request(uri, new HashMap<>(), new HashMap<>()).post(ClientResponse.class);
    }
    return buildResponse(uri, response);
  }

  @Override
  public JSONObject put(String uri, Optional<JSONObject> payload) {
    LOG.debug("Sending PUT '{}': {}", uri, payload);
    ClientResponse response;
    if (payload.isPresent()) {
      response = request(uri, new HashMap<>(), new HashMap<>()).put(ClientResponse.class,
          payload.get().toString());
    } else {
      response = request(uri, new HashMap<>(), new HashMap<>()).put(ClientResponse.class);
    }
    return buildResponse(uri, response);
  }

  private Builder request(String uri, Map<String, String> params,
      Map<String, String> headers) {

    var api = this.client.resource(uri);
    if (params != null) {
      params.forEach(api::queryParam);
    }

    var builtHeaders = buildRequestHeader(headers);
    var builder = api.accept(APPLICATION_JSON);
    if (builtHeaders != null) {
      builtHeaders.forEach(builder::header);
    }
    return builder;
  }
}
