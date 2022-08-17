package com.devit.mscore.web;

import java.util.ArrayList;
import java.util.List;

import com.devit.mscore.AuthenticationProvider;
import com.devit.mscore.Starter;
import com.devit.mscore.Validation;
import com.devit.mscore.exception.ApplicationException;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

/**
 * Server class to create web server.
 * 
 * @author dkakunsi
 */
public abstract class Server implements Starter {

  protected int port;

  protected List<Endpoint> endpoints;

  protected AuthenticationProvider authenticationProvider;

  protected List<Validation> validations;

  protected Server(int port) {
    this.port = port;
    this.validations = new ArrayList<>();
    this.endpoints = new ArrayList<>();
  }

  protected Server(int port, List<Endpoint> endpoints) {
    this(port);
    this.endpoints = endpoints;
  }

  protected int getPort() {
    return this.port;
  }

  protected AuthenticationProvider getAuthenticationProvider() {
    return this.authenticationProvider;
  }

  public void setAuthenticationProvider(AuthenticationProvider authenticationProvider) {
    this.authenticationProvider = authenticationProvider;
  }

  public List<Validation> getValidations() {
    return this.validations;
  }

  public void setValidations(List<Validation> validations) {
    this.validations = validations;
  }

  protected JSONObject createResponseMessage(Exception ex, int statusCode) {
    var exceptionMessage = getMessage(ex);
    var messageType = getMessageType(ex, statusCode);
    return createResponseMessage(exceptionMessage, messageType);
  }

  protected JSONObject createResponseMessage(String message, int statusCode) {
    var messageType = getMessageType(statusCode);
    return createResponseMessage(message, messageType);
  }

  private static JSONObject createResponseMessage(String message, String messageType) {
    return new JSONObject().put("message", message).put("type", messageType);
  }

  private static String getMessage(Throwable ex) {
    var message = ex.getMessage();
    if (ex.getCause() != null) {
      var innerMessage = getMessage(ex.getCause());
      message = StringUtils.isNotBlank(innerMessage) ? innerMessage : message;
    }
    return message;
  }

  private String getMessageType(Exception ex, int statusCode) {
    if (ex instanceof ApplicationException) {
      return getMessageType((ApplicationException) ex, statusCode);
    }
    return getMessageType(statusCode);
  }

  private static String getMessageType(ApplicationException ex, int statusCode) {
    if (StringUtils.isNotEmpty(ex.getType())) {
      return ex.getType();
    }
    return getMessageType(statusCode);
  }

  protected static String getMessageType(int statusCode) {
    return WebUtils.getMessageType(statusCode);
  }
}