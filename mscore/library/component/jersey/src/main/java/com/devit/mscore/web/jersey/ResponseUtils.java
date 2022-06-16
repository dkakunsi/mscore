package com.devit.mscore.web.jersey;

import com.sun.jersey.api.client.ClientResponse;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.ws.rs.core.Response;

public class ResponseUtils {

    private static final Logger LOG = LoggerFactory.getLogger(ResponseUtils.class);

    static JSONObject buildResponse(String uri, Response response) {
        var output = response.hasEntity() ? response.getEntity().toString(): "";
        var status = response.getStatus();
        return buildResponse(uri, output, status);
    }

    static JSONObject buildResponse(String uri, ClientResponse response) {
        var output = response.hasEntity() ? response.getEntity(String.class) : "";
        var status = response.getStatus();
        return buildResponse(uri, output, status);
    }

    static JSONObject buildResponse(String uri, String output, int status) {
        if (status == 404) {
            LOG.error("URI not found: {} - {}", uri, status);
            return buildMessage(status, "Cannot connect to " + uri);
        }

        LOG.debug("Response from {} service: {}", uri, output);
        Object json = extractResponseObject(output);

        if (isError(status)) {
            LOG.error("Failed calling {} - {}: {}", uri, status, output);
            return buildMessage(status, json);
        }
        return buildMessage(status, json);
    }

    private static JSONObject buildMessage(int responseCode, Object payload) {
        return new JSONObject().put("code", responseCode).put("payload", payload);
    }

    private static Object extractResponseObject(String response) {
        if (StringUtils.startsWith(response, "{")) {
            return new JSONObject(response);
        } else if (StringUtils.startsWith(response, "[")) {
            return new JSONArray(response);
        } else {
            return response;
        }
    }

    private static boolean isError(int status) {
        return status >= 400 && status <= 600;
    }
}
