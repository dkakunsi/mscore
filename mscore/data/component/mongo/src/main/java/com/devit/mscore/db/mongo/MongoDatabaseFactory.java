package com.devit.mscore.db.mongo;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.devit.mscore.Configuration;
import com.devit.mscore.Schema;
import com.devit.mscore.exception.ApplicationRuntimeException;
import com.devit.mscore.exception.ConfigException;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoClientSettings.Builder;
import com.mongodb.MongoCredential;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import org.bson.Document;

/**
 *
 * @author dkakunsi
 */
public class MongoDatabaseFactory {

  private static final String CONFIG_TEMPLATE = "platform.mongo.%s";

  private static final String HOST = "host";

  private static final String PORT = "port";

  private static final String DATABASE = "database";

  private static final String SECURE = "secure";

  private static final String USERNAME = "username";

  private static final String PASSWORD = "password";

  private Configuration configuration;

  private Map<String, MongoRepository> repositories;

  private MongoClient client;

  private MongoDatabase mongoDatabase;

  private MongoDatabaseFactory(Configuration configuration) {
    this.configuration = configuration;
    this.repositories = new HashMap<>();
  }

  public static MongoDatabaseFactory of(Configuration configuration) {
    return new MongoDatabaseFactory(configuration);
  }

  public MongoRepository repository(Schema schema) {
    var domain = schema.getDomain();
    this.repositories.computeIfAbsent(domain,
        key -> new MongoRepository(collection(domain), schema.getUniqueAttributes()));
    return this.repositories.get(domain);
  }

  public MongoCollection<Document> collection(String collection) {
    try {
      return database().getCollection(collection);
    } catch (ConfigException ex) {
      throw new ApplicationRuntimeException(ex);
    }
  }

  public MongoDatabase database() throws ConfigException {
    if (this.mongoDatabase == null) {
      var databaseName = getDatabaseName();
      this.mongoDatabase = mongoClient().getDatabase(databaseName);
    }
    return this.mongoDatabase;
  }

  public MongoClient mongoClient() throws ConfigException {
    if (this.client != null) {
      return this.client;
    }

    var databaseName = getDatabaseName();
    var settingsBuilder = MongoClientSettings.builder();
    applyAuthentication(settingsBuilder, databaseName, createConnectionString());
    this.client = MongoClients.create(settingsBuilder.build());
    return this.client;
  }

  private String getDatabaseName() throws ConfigException {
    return getConfig(DATABASE).orElseThrow(() -> new ConfigException("Mongo database is not configured."));
  }

  protected ConnectionString createConnectionString() throws ConfigException {
    var hostname = getHost();
    var portNum = getPort();
    return new ConnectionString(String.format("mongodb://%s:%s", hostname, portNum));
  }

  private String getHost() throws ConfigException {
    return getConfig(HOST).orElseThrow(() -> new ConfigException("Mongo host is not configured."));
  }

  private String getPort() throws ConfigException {
    return getConfig(PORT).orElseThrow(() -> new ConfigException("Mongo port is not configured."));
  }

  protected void applyAuthentication(Builder builder, String databaseName, ConnectionString connectionString)
      throws ConfigException {
    var isSecure = getIsSecure();
    if (Boolean.parseBoolean(isSecure)) {
      var username = getUsername();
      var password = getPassword();
      var credential = MongoCredential.createCredential(username, databaseName, password.toCharArray());
      builder.applyConnectionString(connectionString).credential(credential);
    }
  }

  private String getIsSecure() throws ConfigException {
    return getConfig(SECURE).orElse("false");
  }

  private String getUsername() throws ConfigException {
    return getConfig(USERNAME).orElseThrow(() -> new ConfigException("Mongo username is not provided."));
  }

  private String getPassword() throws ConfigException {
    return getConfig(PASSWORD).orElseThrow(() -> new ConfigException("Mongo password is not provided."));
  }

  private Optional<String> getConfig(String key) throws ConfigException {
    var configName = String.format(CONFIG_TEMPLATE, key);
    return this.configuration.getConfig(configName);
  }

  // TODO remove this after implementing embedded-mongo for testing.
  public void setMongoClient(MongoClient mongoClient) {
    this.client = mongoClient;
  }
}
