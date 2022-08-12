package com.devit.mscore.web.jersey;

import static com.devit.mscore.web.jersey.ResponseUtils.buildResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.devit.mscore.Logger;
import com.devit.mscore.logging.ApplicationLogger;

import org.json.JSONObject;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation.Builder;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

public class JerseyClient implements com.devit.mscore.web.Client, Requester {

    private static final Logger LOG = new ApplicationLogger(JerseyClient.class);

    private Client client;

    JerseyClient(Client client) {
        this.client = client;
    }

    @Override
    public com.devit.mscore.web.Client createNew() {
        return new JerseyClient(ClientBuilder.newClient());
    }

    @Override
    public JSONObject delete(String uri) {
        LOG.debug("Sending DELETE {}", uri);
        var response = request(uri, new HashMap<>(), new HashMap<>()).delete();
        return buildResponse(uri, response);
    }

    @Override
    public JSONObject get(String uri, Map<String, String> params) {
        LOG.debug("Sending GET {}. Entity: {}", uri, params);
        var response = request(uri, params, new HashMap<>()).get();
        return buildResponse(uri, response);
    }

    @Override
    public JSONObject post(String uri, Optional<JSONObject> payload) {
        LOG.debug("Sending POST {}. Entity: {}", uri, payload);
        Response response;
        if (payload.isPresent()) {
            response = request(uri, new HashMap<>(), new HashMap<>()).post(Entity.json(payload.get().toString()));
        } else {
            response = request(uri, new HashMap<>(), new HashMap<>()).post(null);
        }
        return buildResponse(uri, response);
    }

    @Override
    public JSONObject put(String uri, Optional<JSONObject> payload) {
        LOG.debug("Sending PUT {}. Entity: {}", uri, payload);
        Response response;
        if (payload.isPresent()) {
            response = request(uri, new HashMap<>(), new HashMap<>()).put(Entity.json(payload.get().toString()));
        } else {
            response = request(uri, new HashMap<>(), new HashMap<>()).put(null);
        }
        return buildResponse(uri, response);
    }

    private Builder request(String uri, Map<String, String> params,
            Map<String, String> headers) {

        var target = this.client.target(uri);
        if (params != null) {
            params.forEach(target::queryParam);
        }

        var builtHeaders = buildRequestHeader(headers);
        var builder = target.request();
        if (builtHeaders != null) {
            builtHeaders.forEach(builder::header);
        }
        builder.accept(MediaType.APPLICATION_JSON);

        return builder;
    }
}
