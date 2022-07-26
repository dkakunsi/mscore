package com.devit.mscore.web.jersey;

import static com.devit.mscore.ApplicationContext.getContext;
import static com.devit.mscore.util.Constants.ACTION;
import static com.devit.mscore.util.Constants.AUTHORIZATION;
import static com.devit.mscore.util.Constants.BREADCRUMB_ID;
import static com.devit.mscore.util.Constants.EVENT_TYPE;
import static com.devit.mscore.util.Constants.PRINCIPAL;

import com.devit.mscore.Logger;
import com.devit.mscore.logging.ApplicationLogger;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.sun.jersey.api.client.ClientResponse;

import jakarta.ws.rs.core.Response;

public class Requester {

  protected static final String LOG_INFO_FORMAT = "Sending request to '{} {}' with context: '{}'";

  protected final Logger logger = ApplicationLogger.getLogger(getClass());

  protected Map<String, String> buildRequestHeader() {
    var headers = new HashMap<String, String>();
    var context = getContext();
    if (context == null) {
      return headers;
    }

    headers.put(BREADCRUMB_ID, context.getBreadcrumbId());
    context.getToken().ifPresent(token -> headers.put(AUTHORIZATION, token));
    context.getEventType().ifPresent(et -> headers.put(EVENT_TYPE, et));
    context.getAction().ifPresent(a -> headers.put(ACTION, a));

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

  protected Map<String, String> getPrintableHeaders(Map<String, String> headers) {
    var printableheader = new HashMap<>(headers);
    printableheader.remove(AUTHORIZATION);
    return printableheader;
  }

}
