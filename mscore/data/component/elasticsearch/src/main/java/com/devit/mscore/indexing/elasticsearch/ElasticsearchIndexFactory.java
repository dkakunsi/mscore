package com.devit.mscore.indexing.elasticsearch;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import com.devit.mscore.ApplicationContext;
import com.devit.mscore.Configuration;
import com.devit.mscore.Registry;
import com.devit.mscore.Resource;
import com.devit.mscore.ResourceManager;
import com.devit.mscore.exception.ConfigException;
import com.devit.mscore.exception.RegistryException;
import com.devit.mscore.exception.ResourceException;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ElasticsearchIndexFactory extends ResourceManager {

    private static final Logger LOG = LoggerFactory.getLogger(ElasticsearchIndexFactory.class);

    private static final String CONFIG_TEMPLATE = "platform.elasticsearch.%s";

    private static final String HOST = "host";

    private static final String SECURE = "secure";

    private static final String USERNAME = "username";

    private static final String PASSWORD = "password";

    private static final String LOCATION = "services.%s.index.mapping.location";

    private RestHighLevelClient client;

    private ElasticsearchService service;

    protected ElasticsearchIndexFactory(Configuration configuration, Registry registry) {
        super("index_mapping", configuration, registry);
    }

    public static ElasticsearchIndexFactory of(Configuration configuration, Registry registry) {
        return new ElasticsearchIndexFactory(configuration, registry);
    }

    public ElasticsearchIndex index(ApplicationContext context, String indexName) throws ConfigException, RegistryException {
        var mapping = this.registry.get(context, indexName);
        return new ElasticsearchIndex(indexName, service(context), new JSONObject(mapping));
    }

    protected ElasticsearchService service(ApplicationContext context) throws ConfigException {
        if (this.service == null) {
            this.service = new ElasticsearchService(getESClient(context));
        }
        return this.service;
    }

    private RestHighLevelClient getESClient(ApplicationContext context) throws ConfigException {
        if (this.client != null) {
            return this.client;
        }

        var builder = RestClient.builder(getHost(context));
        if (isSecure(context)) {
            LOG.info("Applying secure connection to ES.");
            applyAuthentication(context, builder);
        }
        this.client = new RestHighLevelClient(builder);
        return this.client;
    }

    // TODO remove this after implementing embedded-es for testing.
    public void setESClient(RestHighLevelClient client) {
        this.client = client;
    }

    protected void applyAuthentication(ApplicationContext context, RestClientBuilder restClientBuilder) throws ConfigException {
        var username = getUsername(context);
        var password = getPassword(context);
        final var credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));

        restClientBuilder.setHttpClientConfigCallback(httpClientBuilder -> {
            return httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
        });
    }

    protected HttpHost[] getHost(ApplicationContext context) throws ConfigException {
        var addresses = this.configuration.getConfig(context, getConfigName(HOST)).orElseThrow(() -> new ConfigException("No ES host is configured.")).split(",");
        var hosts = new HttpHost[addresses.length];

        LOG.debug("Trying to connect to ES host: {}", (Object[]) addresses);

        try {
            var i = 0;
            for (var address : addresses) {
                var url = new URL(address);
                hosts[i] = new HttpHost(url.getHost(), url.getPort(), url.getProtocol());
                i++;
            }
        } catch (MalformedURLException ex) {
            throw new ConfigException(String.format("Cannot create host: %s.", (Object[]) addresses), ex);
        }
        return hosts;
    }

    protected boolean isSecure(ApplicationContext context) {
        try {
            var secureConfig = this.configuration.getConfig(context, getConfigName(SECURE)).orElse("false");
            return Boolean.parseBoolean(secureConfig);
        } catch (ConfigException ex) {
            return false;
        }
    }

    protected String getUsername(ApplicationContext context) throws ConfigException {
        var username = this.configuration.getConfig(context, getConfigName(USERNAME));
        return username.orElseThrow(() -> new ConfigException("ES username is not provided"));
    }

    protected String getPassword(ApplicationContext context) throws ConfigException {
        var password = this.configuration.getConfig(context, getConfigName(PASSWORD));
        return password.orElseThrow(() -> new ConfigException("ES password is not provided"));
    }

    private String getConfigName(String key) {
        return String.format(CONFIG_TEMPLATE, key);
    }

    @Override
    protected String getResourceLocation(ApplicationContext context) {
        var configName = String.format(LOCATION, this.configuration.getServiceName());
        try {
            return this.configuration.getConfig(context, configName).orElse(null);
        } catch (ConfigException e) {
            return null;
        }
    }

    @Override
    protected Resource createResource(File file) throws ResourceException {
        return new ElasticsearchMapping(file);
    }
}
