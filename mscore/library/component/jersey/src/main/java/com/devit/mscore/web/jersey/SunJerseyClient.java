package com.devit.mscore.web.jersey;

import static com.devit.mscore.util.Utils.BREADCRUMB_ID;
import static com.devit.mscore.web.jersey.ResponseUtils.buildResponse;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.devit.mscore.ApplicationContext;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource.Builder;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SunJerseyClient implements com.devit.mscore.web.Client, Requester {

    private static final Logger LOG = LoggerFactory.getLogger(SunJerseyClient.class);

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
    public JSONObject delete(ApplicationContext context, String uri) {
        LOG.debug("BreadcrumbId: {}. Sending DELETE '{}'", context.getBreadcrumbId(), uri);
        var response = request(context, uri, new HashMap<>(), new HashMap<>()).delete(ClientResponse.class);
        return buildResponse(uri, response);
    }

    @Override
    public JSONObject get(ApplicationContext context, String uri, Map<String, String> params) {
        LOG.debug("BreadcrumbId: {}. Sending GET '{}' with params: {}", context.getBreadcrumbId(), uri, params);
        var response = request(context, uri, params, new HashMap<>()).get(ClientResponse.class);
        return buildResponse(uri, response);
    }

    @Override
    public JSONObject post(ApplicationContext context, String uri, Optional<JSONObject> payload) {
        LOG.debug("BreadcrumbId: {}. Sending POST '{}': {}", context.getBreadcrumbId(), uri, payload);
        ClientResponse response;
        if (payload.isPresent()) {
            response = request(context, uri, new HashMap<>(), new HashMap<>()).post(ClientResponse.class,
                    payload.get().toString());
        } else {
            response = request(context, uri, new HashMap<>(), new HashMap<>()).post(ClientResponse.class);
        }
        return buildResponse(uri, response);
    }

    @Override
    public JSONObject post(String uri, JSONObject payload, Map<String, String> headers) {
        LOG.debug("BreadcrumbId: {}. Sending POST '{}'. Payload: {}. Headers: {}", headers.get(BREADCRUMB_ID), uri,
                payload, headers);
        var response = request(null, uri, new HashMap<>(), headers).post(ClientResponse.class, payload.toString());
        return buildResponse(uri, response);
    }

    @Override
    public JSONObject put(ApplicationContext context, String uri, Optional<JSONObject> payload) {
        LOG.debug("BreadcrumbId: {}. Sending PUT '{}': {}", context.getBreadcrumbId(), uri, payload);
        ClientResponse response;
        if (payload.isPresent()) {
            response = request(context, uri, new HashMap<>(), new HashMap<>()).put(ClientResponse.class,
                    payload.get().toString());
        } else {
            response = request(context, uri, new HashMap<>(), new HashMap<>()).put(ClientResponse.class);
        }
        return buildResponse(uri, response);
    }

    @Override
    public JSONObject put(String uri, JSONObject payload, Map<String, String> headers) {
        LOG.debug("BreadcrumbId: {}. Sending PUT '{}'. Payload: {}. Headers {}", headers.get(BREADCRUMB_ID), uri,
                payload, headers);
        var response = request(null, uri, new HashMap<>(), headers).put(ClientResponse.class, payload.toString());
        return buildResponse(uri, response);
    }

    private Builder request(ApplicationContext context, String uri, Map<String, String> params,
            Map<String, String> headers) {

        var api = this.client.resource(uri);
        if (params != null) {
            params.forEach(api::queryParam);
        }

        var builtHeaders = buildRequestHeader(context, headers);
        var builder = api.accept(APPLICATION_JSON);
        if (builtHeaders != null) {
            builtHeaders.forEach(builder::header);
        }
        return builder;
    }
}
