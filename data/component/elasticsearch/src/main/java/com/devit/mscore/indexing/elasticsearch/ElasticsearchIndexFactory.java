package com.devit.mscore.indexing.elasticsearch;

import com.devit.mscore.Configuration;
import com.devit.mscore.Logger;
import com.devit.mscore.Registry;
import com.devit.mscore.Resource;
import com.devit.mscore.ResourceManager;
import com.devit.mscore.exception.ApplicationRuntimeException;
import com.devit.mscore.exception.ConfigException;
import com.devit.mscore.exception.RegistryException;
import com.devit.mscore.exception.ResourceException;
import com.devit.mscore.logging.ApplicationLogger;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.json.JSONObject;

public class ElasticsearchIndexFactory extends ResourceManager {

  private static final Logger LOG = new ApplicationLogger(ElasticsearchIndexFactory.class);

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

  public ElasticsearchIndex index(String indexName) throws RegistryException {
    var mapping = this.registry.get(indexName);
    return new ElasticsearchIndex(indexName, service(), new JSONObject(mapping));
  }

  protected ElasticsearchService service() {
    if (this.service == null) {
      this.service = new ElasticsearchService(() -> {
        try {
          return getESClient();
        } catch (ConfigException ex) {
          throw new ApplicationRuntimeException(ex);
        }
      });
    }
    return this.service;
  }

  private RestHighLevelClient getESClient() throws ConfigException {
    if (this.client != null) {
      return this.client;
    }
    return this.client = Helper.getEsClient(configuration, getHost(), isSecure());
  }

  protected HttpHost[] getHost() throws ConfigException {
    var addresses = getConfigValue(this.configuration, HOST).orElseThrow(() -> new ConfigException("No ES host is configured.")).split(",");
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

  protected boolean isSecure() {
    try {
      var secureConfig = getConfigValue(this.configuration, SECURE).orElse("false");
      return Boolean.parseBoolean(secureConfig);
    } catch (ConfigException ex) {
      return false;
    }
  }

  @Override
  protected String getResourceLocation() {
    var configName = String.format(LOCATION, this.configuration.getServiceName());
    try {
      return this.configuration.getConfig(configName).orElse(null);
    } catch (ConfigException e) {
      return null;
    }
  }

  @Override
  protected Resource createResource(File file) throws ResourceException {
    return new ElasticsearchMapping(file);
  }

  private static Optional<String> getConfigValue(Configuration configuration, String key) throws ConfigException {
    return configuration.getConfig(String.format(CONFIG_TEMPLATE, key));
  }

  protected static class Helper {

    protected static RestHighLevelClient getEsClient(Configuration configuration, HttpHost[] hosts, boolean isSecure) throws ConfigException {
      var builder = RestClient.builder(hosts);
      if (isSecure) {
        LOG.info("Applying secure connection to ES.");
        applyAuthentication(configuration, builder);
      }
      return new RestHighLevelClient(builder);
    }
  
    protected static void applyAuthentication(Configuration configuration, RestClientBuilder restClientBuilder) throws ConfigException {
      var username = getUsername(configuration);
      var password = getPassword(configuration);
      final var credentialsProvider = new BasicCredentialsProvider();
      credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
  
      restClientBuilder.setHttpClientConfigCallback(
          httpClientBuilder -> httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider));
    }
  
    protected static String getUsername(Configuration configuration) throws ConfigException {
      return getConfigValue(configuration, USERNAME).orElseThrow(() -> new ConfigException("ES username is not provided"));
    }
  
    protected static String getPassword(Configuration configuration) throws ConfigException {
      return getConfigValue(configuration, PASSWORD).orElseThrow(() -> new ConfigException("ES password is not provided"));
    }
  }
}