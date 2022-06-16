package com.devit.mscore.web;

import java.util.Map;
import java.util.Optional;

import com.devit.mscore.ApplicationContext;
import com.devit.mscore.exception.WebClientException;

import org.json.JSONObject;

/**
 * Object to communicate with external service using HTTP.
 * 
 * @author dkakunsi
 */
public interface Client {

    Client createNew();

    JSONObject post(ApplicationContext context, String uri, Optional<JSONObject> payload) throws WebClientException;

    JSONObject put(ApplicationContext context, String uri, Optional<JSONObject> payload) throws WebClientException;

    JSONObject delete(ApplicationContext context, String uri) throws WebClientException;

    JSONObject get(ApplicationContext context, String uri, Map<String, String> params) throws WebClientException;

    /**
     * @deprecated
     */
    @Deprecated(forRemoval = true)
    default JSONObject post(String uri, JSONObject payload, Map<String, String> headers) throws WebClientException {
        return null;
    }

    /**
     * @deprecated
     */
    @Deprecated(forRemoval = true)
    default JSONObject put(String uri, JSONObject payload, Map<String, String> headers) throws WebClientException {
        return null;
    }
}
