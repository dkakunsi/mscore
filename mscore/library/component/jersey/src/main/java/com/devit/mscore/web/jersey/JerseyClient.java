package com.devit.mscore.web.jersey;

import static com.devit.mscore.web.jersey.ResponseUtils.buildResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.devit.mscore.ApplicationContext;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation.Builder;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

public class JerseyClient implements com.devit.mscore.web.Client, Requester {

    private static final Logger LOG = LoggerFactory.getLogger(JerseyClient.class);

    private Client client;

    JerseyClient(Client client) {
        this.client = client;
    }

    @Override
    public com.devit.mscore.web.Client createNew() {
        return new JerseyClient(ClientBuilder.newClient());
    }

    @Override
    public JSONObject delete(ApplicationContext context, String uri) {
        LOG.debug("BreadcrumbId: {}. Sending DELETE {}", context.getBreadcrumbId(), uri);
        var response = request(context, uri, new HashMap<>(), new HashMap<>()).delete();
        return buildResponse(uri, response);
    }

    @Override
    public JSONObject get(ApplicationContext context, String uri, Map<String, String> params) {
        LOG.debug("BreadcrumbId: {}. Sending GET {}. Entity: {}", context.getBreadcrumbId(), uri, params);
        var response = request(context, uri, params, new HashMap<>()).get();
        return buildResponse(uri, response);
    }

    @Override
    public JSONObject post(ApplicationContext context, String uri, Optional<JSONObject> payload) {
        LOG.debug("BreadcrumbId: {}. Sending POST {}. Entity: {}", context.getBreadcrumbId(), uri, payload);
        Response response;
        if (payload.isPresent()) {
            response = request(context, uri, new HashMap<>(), new HashMap<>()).post(Entity.json(payload.get().toString()));
        } else {
            response = request(context, uri, new HashMap<>(), new HashMap<>()).post(null);
        }
        return buildResponse(uri, response);
    }

    @Override
    public JSONObject put(ApplicationContext context, String uri, Optional<JSONObject> payload) {
        LOG.debug("BreadcrumbId: {}. Sending PUT {}. Entity: {}", context.getBreadcrumbId(), uri, payload);
        Response response;
        if (payload.isPresent()) {
            response = request(context, uri, new HashMap<>(), new HashMap<>()).put(Entity.json(payload.get().toString()));
        } else {
            response = request(context, uri, new HashMap<>(), new HashMap<>()).put(null);
        }
        return buildResponse(uri, response);
    }

    private Builder request(ApplicationContext context, String uri, Map<String, String> params,
            Map<String, String> headers) {

        var target = this.client.target(uri);
        if (params != null) {
            params.forEach(target::queryParam);
        }

        var builtHeaders = buildRequestHeader(context, headers);
        var builder = target.request();
        if (builtHeaders != null) {
            builtHeaders.forEach(builder::header);
        }
        builder.accept(MediaType.APPLICATION_JSON);

        return builder;
    }
}
