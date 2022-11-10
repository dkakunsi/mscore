package com.devit.mscore.web.jersey;

import static com.devit.mscore.util.Constants.AUTHORIZATION;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.ws.rs.HttpMethod;

import org.json.JSONObject;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource.Builder;

public class SunJerseyClient extends Requester implements com.devit.mscore.web.Client {

  private Client client;

  SunJerseyClient() {
    this(Client.create());
  }

  SunJerseyClient(Client client) {
    this.client = client;
  }

  @Override
  public JSONObject delete(String uri) {
    var response = request(HttpMethod.DELETE, uri, new HashMap<>(), new HashMap<>()).delete(ClientResponse.class);
    return buildResponse(uri, response);
  }

  @Override
  public JSONObject get(String uri, Map<String, String> params) {
    var response = request(HttpMethod.GET, uri, params, new HashMap<>()).get(ClientResponse.class);
    return buildResponse(uri, response);
  }

  @Override
  public JSONObject post(String uri, Optional<JSONObject> payload) {
    ClientResponse response;
    if (payload.isPresent()) {
      response = request(HttpMethod.POST, uri, new HashMap<>(), new HashMap<>()).post(ClientResponse.class,
          payload.get().toString());
    } else {
      response = request(HttpMethod.POST, uri, new HashMap<>(), new HashMap<>()).post(ClientResponse.class);
    }
    return buildResponse(uri, response);
  }

  @Override
  public JSONObject put(String uri, Optional<JSONObject> payload) {
    ClientResponse response;
    if (payload.isPresent()) {
      response = request(HttpMethod.PUT, uri, new HashMap<>(), new HashMap<>()).put(ClientResponse.class,
          payload.get().toString());
    } else {
      response = request(HttpMethod.PUT, uri, new HashMap<>(), new HashMap<>()).put(ClientResponse.class);
    }
    return buildResponse(uri, response);
  }

  private Builder request(String method, String uri, Map<String, String> params,
      Map<String, String> headers) {

    var api = client.resource(uri);
    if (params != null) {
      params.forEach(api::queryParam);
    }

    var builtHeaders = buildRequestHeader(headers);
    var builder = api.accept(APPLICATION_JSON);
    if (builtHeaders != null) {
      builtHeaders.forEach(builder::header);
    }
    logger.info(LOG_INFO_FORMAT, method, uri, getPrintableHeaders(builtHeaders));
    return builder;
  }

  private Map<String, String> getPrintableHeaders(Map<String, String> headers) {
    var printableheader = new HashMap<>(headers);
    printableheader.remove(AUTHORIZATION);
    return printableheader;
  }
}
