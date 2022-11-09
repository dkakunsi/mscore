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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.json.JSONObject;

public class ElasticsearchIndexFactory extends ResourceManager {

  private static final Logger LOG = ApplicationLogger.getLogger(ElasticsearchIndexFactory.class);

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
    var mapping = registry.get(indexName);
    return new ElasticsearchIndex(indexName, service(), new JSONObject(mapping));
  }

  protected ElasticsearchService service() {
    if (service == null) {
      service = new ElasticsearchService(() -> {
        try {
          return getESClient();
        } catch (ConfigException ex) {
          throw new ApplicationRuntimeException(ex);
        }
      });
    }
    return service;
  }

  private RestHighLevelClient getESClient() throws ConfigException {
    if (client != null) {
      return client;
    }
    return client = Helper.getEsClient(configuration, getHost(), isSecure());
  }

  protected HttpHost[] getHost() throws ConfigException {
    var addresses = getConfigValue(configuration, HOST)
        .orElseThrow(() -> new ConfigException("No ES host is configured")).split(",");
    var hosts = new ArrayList<HttpHost>();
    LOG.info("Connecting to ES on '{}'", List.of(addresses));
    Stream.of(addresses).forEach(a -> hosts.add(HttpHost.create(a)));
    return hosts.toArray(new HttpHost[0]);
  }

  protected boolean isSecure() {
    try {
      var secureConfig = getConfigValue(configuration, SECURE).orElse("false");
      return Boolean.parseBoolean(secureConfig);
    } catch (ConfigException ex) {
      return false;
    }
  }

  @Override
  protected String getResourceLocation() {
    var configName = String.format(LOCATION, configuration.getServiceName());
    try {
      return configuration.getConfig(configName).orElse(null);
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

    protected static RestHighLevelClient getEsClient(Configuration configuration, HttpHost[] hosts, boolean isSecure)
        throws ConfigException {
      var builder = RestClient.builder(hosts);
      if (isSecure) {
        LOG.info("Applying secure connection to ES");
        applyAuthentication(configuration, builder);
      }
      return new RestHighLevelClient(builder);
    }

    protected static void applyAuthentication(Configuration configuration, RestClientBuilder restClientBuilder)
        throws ConfigException {
      var username = getUsername(configuration);
      var password = getPassword(configuration);
      final var credentialsProvider = new BasicCredentialsProvider();
      credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));

      restClientBuilder.setHttpClientConfigCallback(
          httpClientBuilder -> httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider));
    }

    protected static String getUsername(Configuration configuration) throws ConfigException {
      return getConfigValue(configuration, USERNAME)
          .orElseThrow(() -> new ConfigException("ES username is not provided"));
    }

    protected static String getPassword(Configuration configuration) throws ConfigException {
      return getConfigValue(configuration, PASSWORD)
          .orElseThrow(() -> new ConfigException("ES password is not provided"));
    }
  }
}
