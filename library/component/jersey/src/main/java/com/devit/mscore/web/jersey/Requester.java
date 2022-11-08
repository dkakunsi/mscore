package com.devit.mscore.web.jersey;

import static com.devit.mscore.ApplicationContext.getContext;
import static com.devit.mscore.util.Utils.AUTHORIZATION;
import static com.devit.mscore.util.Utils.BREADCRUMB_ID;
import static com.devit.mscore.util.Utils.EVENT_TYPE;
import static com.devit.mscore.util.Utils.PRINCIPAL;

import com.devit.mscore.Logger;
import com.devit.mscore.logging.ApplicationLogger;

import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.sun.jersey.api.client.ClientResponse;

import jakarta.ws.rs.core.Response;


public class Requester {

  protected Logger logger = ApplicationLogger.getLogger(this.getClass());

  protected Map<String, String> buildRequestHeader(Map<String, String> headers) {
    var context = getContext();
    if (context == null) {
      return headers;
    }

    headers.put(BREADCRUMB_ID, context.getBreadcrumbId());
    context.getToken().ifPresent(token -> headers.put(AUTHORIZATION, token));
    context.getEventType().ifPresent(et -> headers.put(EVENT_TYPE, et));

    // principal can be empty when the request is login request.
    context.getPrincipal().ifPresent(principal -> headers.put(PRINCIPAL, principal.toString()));

    return headers;
  }

  protected JSONObject buildResponse(String uri, ClientResponse response) {
    var output = response.hasEntity() ? response.getEntity(String.class) : "";
    var status = response.getStatus();
    return buildResponse(uri, output, status);
  }

  protected JSONObject buildResponse(String uri, Response response) {
    var output = response.hasEntity() ? response.getEntity().toString() : "";
    var status = response.getStatus();
    return buildResponse(uri, output, status);
  }

  protected JSONObject buildResponse(String uri, String output, int status) {
    if (status == 404) {
      logger.error("URI not found: {} - {}", uri, status);
      return buildMessage(status, "Cannot connect to " + uri);
    }

    logger.debug("Response from '{}' service: '{}'", uri, output);
    Object json = extractResponseObject(output);

    if (isError(status)) {
      logger.error("Failed calling {} - {}: {}", uri, status, output);
      return buildMessage(status, json);
    }
    return buildMessage(status, json);
  }

  private static JSONObject buildMessage(int responseCode, Object payload) {
    return new JSONObject().put("code", responseCode).put("payload", payload);
  }

  private static Object extractResponseObject(String response) {
    if (response.charAt(0) == '{') {
      return new JSONObject(response);
    } else if (response.charAt(0) == '[') {
      return new JSONArray(response);
    } else {
      return response;
    }
  }

  private static boolean isError(int status) {
    return status >= 400 && status <= 600;
  }
}
