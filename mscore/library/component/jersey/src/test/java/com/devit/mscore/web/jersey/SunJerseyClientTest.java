package com.devit.mscore.web.jersey;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.devit.mscore.ApplicationContext;
import com.devit.mscore.DefaultApplicationContext;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

public class SunJerseyClientTest {

    private SunJerseyClient webClient;

    private Client client;

    private WebResource api;

    private Builder builder;

    private ApplicationContext context;

    @Before
    public void setup() {
        this.builder = mock(Builder.class);
        this.api = mock(WebResource.class);
        doReturn(this.builder).when(this.api).accept(anyString());

        this.client = mock(Client.class);
        doReturn(this.api).when(this.client).resource(anyString());

        this.webClient = new SunJerseyClient(this.client);

        var contextData = new HashMap<String, Object>(); 
        contextData.put("principal", new JSONObject("{\"requestedBy\":\"requestedBy\",\"role\":[\"user\"]}"));
        this.context = DefaultApplicationContext.of("test", contextData);
    }

    @Test
    public void testDelete() {
        var response = mock(ClientResponse.class);
        doReturn(true).when(response).hasEntity();
        doReturn("deleted").when(response).getEntity(String.class);
        doReturn(200).when(response).getStatus();
        doReturn(response).when(this.builder).delete(ClientResponse.class);

        var result = this.webClient.delete(this.context, "uri");

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

        var result = this.webClient.get(this.context, "uri", Map.of("Authorization", "auth"));

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

        var result = this.webClient.post(this.context, "uri", Optional.of(new JSONObject("{\"id\":\"id\"}")));

        assertNotNull(result);
        assertThat(result.length(), is(2));
        assertThat(result.getInt("code"), is(500));
        assertThat(result.getJSONObject("payload").getString("message"), is("Internal Server Error"));
    }

    @Test
    public void testPost_withParams() {
        var response = mock(ClientResponse.class);
        doReturn(true).when(response).hasEntity();
        doReturn("{\"message\":\"Internal Server Error\"}").when(response).getEntity(String.class);
        doReturn(500).when(response).getStatus();
        doReturn(response).when(this.builder).post(ClientResponse.class, "{\"id\":\"id\"}");

        var params = Map.of("param1", "value1");
        var result = this.webClient.post("uri", new JSONObject("{\"id\":\"id\"}"), params);

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

        var result = this.webClient.put(this.context, "uri", Optional.of(new JSONObject("{\"id\":\"id\"}")));

        assertNotNull(result);
        assertThat(result.length(), is(2));
        assertThat(result.getInt("code"), is(400));
        assertThat(result.getJSONObject("payload").getString("message"), is("Invalid Message"));
    }

    @Test
    public void testPut_withParam() {
        var response = mock(ClientResponse.class);
        doReturn(true).when(response).hasEntity();
        doReturn("{\"message\":\"Invalid Message\"}").when(response).getEntity(String.class);
        doReturn(400).when(response).getStatus();
        doReturn(response).when(this.builder).put(ClientResponse.class, "{\"id\":\"id\"}");

        var params = Map.of("param1", "value1");
        var result = this.webClient.put("uri", new JSONObject("{\"id\":\"id\"}"), params);

        assertNotNull(result);
        assertThat(result.length(), is(2));
        assertThat(result.getInt("code"), is(400));
        assertThat(result.getJSONObject("payload").getString("message"), is("Invalid Message"));
    }

    @Test
    public void testClone() {
        var clone = this.webClient.createNew();
        assertNotEquals(clone, this.webClient);
    }
}
