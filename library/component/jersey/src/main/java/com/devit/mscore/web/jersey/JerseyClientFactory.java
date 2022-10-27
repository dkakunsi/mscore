package com.devit.mscore.web.jersey;

import com.devit.mscore.web.Client;

import jakarta.ws.rs.client.ClientBuilder;

public class JerseyClientFactory {

  public static JerseyClientFactory of() {
    return new JerseyClientFactory();
  }

  public Client client() {
    var httpClient = ClientBuilder.newClient();
    return new JerseyClient(httpClient);
  }
}
