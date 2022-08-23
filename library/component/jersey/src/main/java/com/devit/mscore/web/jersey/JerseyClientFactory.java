package com.devit.mscore.web.jersey;

import com.devit.mscore.web.Client;

public class JerseyClientFactory {

  public static JerseyClientFactory of() {
    return new JerseyClientFactory();
  }

  public Client client() {
    return new SunJerseyClient();
  }
}
