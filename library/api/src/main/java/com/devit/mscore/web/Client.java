package com.devit.mscore.web;

import com.devit.mscore.exception.WebClientException;

import java.util.Map;
import java.util.Optional;

import org.json.JSONObject;

/**
 * Object to communicate with external service using HTTP.
 *
 * @author dkakunsi
 */
public interface Client {

  JSONObject post(String uri, Optional<JSONObject> payload) throws WebClientException;

  JSONObject put(String uri, Optional<JSONObject> payload) throws WebClientException;

  JSONObject delete(String uri) throws WebClientException;

  JSONObject get(String uri, Map<String, String> params) throws WebClientException;
}
