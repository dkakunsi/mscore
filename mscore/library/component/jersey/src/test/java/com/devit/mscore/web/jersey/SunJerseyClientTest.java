package com.devit.mscore.web.jersey;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.Map;
import java.util.Optional;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;

public class SunJerseyClientTest {

  private SunJerseyClient webClient;

  private Client client;

  private WebResource api;

  private Builder builder;

  @Before
  public void setup() {
    this.builder = mock(Builder.class);
    this.api = mock(WebResource.class);
    doReturn(this.builder).when(this.api).accept(anyString());

    this.client = mock(Client.class);
    doReturn(this.api).when(this.client).resource(anyString());

    this.webClient = new SunJerseyClient(this.client);
  }

  @Test
  public void testDelete() {
    var response = mock(ClientResponse.class);
    doReturn(true).when(response).hasEntity();
    doReturn("deleted").when(response).getEntity(String.class);
    doReturn(200).when(response).getStatus();
    doReturn(response).when(this.builder).delete(ClientResponse.class);

    var result = this.webClient.delete("uri");

    assertNotNull(result);
    assertThat(result.length(), is(2));
    assertThat(result.getInt("code"), is(200));
    assertThat(result.getString("payload"), is("deleted"));
  }

  @Test
  public void testGet() {
    var response = mock(ClientResponse.class);
    doReturn(false).when(response).hasEntity();
    doReturn(404).when(response).getStatus();
    doReturn(response).when(this.builder).get(ClientResponse.class);

    var result = this.webClient.get("uri", Map.of("Authorization", "auth"));

    assertNotNull(result);
    assertThat(result.length(), is(2));
    assertThat(result.getInt("code"), is(404));
    assertThat(result.getString("payload"), is("Cannot connect to uri"));
  }

  @Test
  public void testPost() {
    var response = mock(ClientResponse.class);
    doReturn(true).when(response).hasEntity();
    doReturn("{\"message\":\"Internal Server Error\"}").when(response).getEntity(String.class);
    doReturn(500).when(response).getStatus();
    doReturn(response).when(this.builder).post(ClientResponse.class, "{\"id\":\"id\"}");

    var result = this.webClient.post("uri", Optional.of(new JSONObject("{\"id\":\"id\"}")));

    assertNotNull(result);
    assertThat(result.length(), is(2));
    assertThat(result.getInt("code"), is(500));
    assertThat(result.getJSONObject("payload").getString("message"), is("Internal Server Error"));
  }

  @Test
  public void testPut() {
    var response = mock(ClientResponse.class);
    doReturn(true).when(response).hasEntity();
    doReturn("{\"message\":\"Invalid Message\"}").when(response).getEntity(String.class);
    doReturn(400).when(response).getStatus();
    doReturn(response).when(this.builder).put(ClientResponse.class, "{\"id\":\"id\"}");

    var result = this.webClient.put("uri", Optional.of(new JSONObject("{\"id\":\"id\"}")));

    assertNotNull(result);
    assertThat(result.length(), is(2));
    assertThat(result.getInt("code"), is(400));
    assertThat(result.getJSONObject("payload").getString("message"), is("Invalid Message"));
  }

  @Test
  public void testClone() throws CloneNotSupportedException {
    var clone = this.webClient.clone();
    assertNotEquals(clone, this.webClient);
  }
}
