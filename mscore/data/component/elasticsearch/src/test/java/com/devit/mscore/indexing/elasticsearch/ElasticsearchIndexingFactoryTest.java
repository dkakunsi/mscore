package com.devit.mscore.indexing.elasticsearch;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Optional;

import com.devit.mscore.Configuration;
import com.devit.mscore.Registry;
import com.devit.mscore.exception.ConfigException;
import com.devit.mscore.exception.RegistryException;
import com.devit.mscore.exception.ResourceException;

import org.elasticsearch.client.IndicesClient;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class ElasticsearchIndexingFactoryTest {

    private ElasticsearchIndexFactory manager;

    private Registry registry;

    private Configuration configuration;

    private RestHighLevelClient client;

    @Before
    public void setup() {
        this.configuration = mock(Configuration.class);
        doReturn("data").when(this.configuration).getServiceName();
        doReturn(true).when(this.configuration).has("services.data.index.mapping.location");

        this.registry = mock(Registry.class);
        this.client = mock(RestHighLevelClient.class);
        this.manager = ElasticsearchIndexFactory.of(this.configuration, this.registry);
        this.manager.setESClient(this.client);
    }

    @Test
    public void testGetIndex() throws ConfigException, IOException, RegistryException {
        var indices = mock(IndicesClient.class);
        doReturn(false).when(indices).exists(any(GetIndexRequest.class), any(RequestOptions.class));
        doReturn(new CreateIndexResponse(true, false, null)).when(indices).create(any(CreateIndexRequest.class),
                any(RequestOptions.class));
        doReturn(indices).when(this.client).indices();
        doReturn("{\"a\":\"A\"}").when(this.registry).get(any());

        var index = this.manager.index("indexName");
        assertNotNull(index);
    }

    @Test
    public void testGetHost() throws ConfigException {
        doReturn(Optional.of("http://ref-es:9200")).when(this.configuration).getConfig("platform.elasticsearch.host");
        var hosts = this.manager.getHost();
        assertNotNull(hosts);
        assertThat(hosts.length, is(1));
        assertThat(hosts[0].getSchemeName(), is("http"));
        assertThat(hosts[0].getHostName(), is("ref-es"));
        assertThat(hosts[0].getPort(), is(9200));
    }

    @Test
    public void testGetHost_MalformedURL() throws ConfigException {
        doReturn(Optional.of("###")).when(this.configuration).getConfig("platform.elasticsearch.host");
        var ex = assertThrows(ConfigException.class, () -> this.manager.getHost());
        assertThat(ex.getMessage(), is("Cannot create host: ###."));
        assertThat(ex.getCause(), instanceOf(MalformedURLException.class));
    }

    @Test
    public void testGetHost_NotProvided() throws ConfigException {
        doReturn(Optional.empty()).when(this.configuration).getConfig("platform.elasticsearch.host");
        var ex = assertThrows(ConfigException.class, () -> this.manager.getHost());
        assertThat(ex.getMessage(), is("No ES host is configured."));
    }

    @Test
    public void testApplyAuthentication() throws ConfigException {
        doReturn(Optional.of("true")).when(this.configuration).getConfig("platform.elasticsearch.secure");
        doReturn(Optional.of("username")).when(this.configuration).getConfig("platform.elasticsearch.username");
        doReturn(Optional.of("password")).when(this.configuration).getConfig("platform.elasticsearch.password");

        var restClientBuilder = mock(RestClientBuilder.class);
        this.manager.applyAuthentication(restClientBuilder);

        verify(restClientBuilder, times(1)).setHttpClientConfigCallback(any());
    }

    @Test
    public void testIsSecure_Exception() throws ConfigException {
        doReturn(Optional.empty()).when(this.configuration).getConfig("platform.elasticsearch.secure");
        assertFalse(this.manager.isSecure());
    }

    @Test
    public void testGetUsername_NotProvidedWhenSecure() throws ConfigException {
        doReturn(Optional.of("true")).when(this.configuration).getConfig("platform.elasticsearch.secure");
        doReturn(Optional.empty()).when(this.configuration).getConfig("platform.elasticsearch.username");
        var ex = assertThrows(ConfigException.class, () -> this.manager.getUsername());
        assertThat(ex.getMessage(), is("ES username is not provided"));
    }

    @Test
    public void testGetUsername_NotProvidedWhenNotSecure() throws ConfigException {
        doReturn(Optional.of("false")).when(this.configuration).getConfig("platform.elasticsearch.secure");
        doReturn(Optional.empty()).when(this.configuration).getConfig("platform.elasticsearch.username");
        var ex = assertThrows(ConfigException.class, () -> this.manager.getUsername());
        assertThat(ex.getMessage(), is("ES username is not provided"));
    }

    @Test
    public void testGetPassword_NotProvidedWhenSecure() throws ConfigException {
        doReturn(Optional.of("true")).when(this.configuration).getConfig("platform.elasticsearch.secure");
        doReturn(Optional.empty()).when(this.configuration).getConfig("platform.elasticsearch.password");
        var ex = assertThrows(ConfigException.class, () -> this.manager.getPassword());
        assertThat(ex.getMessage(), is("ES password is not provided"));
    }

    @Test
    public void testGetPassword_NotProvidedWhenNotSecure() throws ConfigException {
        doReturn(Optional.of("false")).when(this.configuration).getConfig("platform.elasticsearch.secure");
        doReturn(Optional.empty()).when(this.configuration).getConfig("platform.elasticsearch.password");
        var ex = assertThrows(ConfigException.class, () -> this.manager.getPassword());
        assertThat(ex.getMessage(), is("ES password is not provided"));
    }

    @Test
    public void testRegisterMapping() throws ConfigException, ResourceException, RegistryException {
        var location = getLocation("resource");
        doReturn(Optional.of(location)).when(this.configuration).getConfig("services.data.index.mapping.location");

        this.manager.registerResources();

        var captor = ArgumentCaptor.forClass(String.class);
        verify(this.registry, times(1)).add(anyString(), captor.capture());

        var argument = new JSONObject(captor.getValue());
        assertThat(argument.getString("name"), is("resource"));
        assertThat(argument.getString("content"), is("{\"id\":\"id\"}"));
    }

    private String getLocation(String location) {
        var classLoader = getClass().getClassLoader();
        var resource = classLoader.getResource(location);
        var file = new File(resource.getFile());
        return file.getAbsolutePath();
    }
}
