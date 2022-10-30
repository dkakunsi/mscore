package com.devit.mscore.web.jersey;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class JerseyClientFactoryTest {

  @Test
  public void test() {
    var factory = JerseyClientFactory.of();
    var client = factory.client();

    assertTrue(client instanceof SunJerseyClient);
  }
}
