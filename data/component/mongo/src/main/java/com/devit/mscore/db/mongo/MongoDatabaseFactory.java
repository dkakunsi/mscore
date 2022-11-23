package com.devit.mscore.db.mongo;

import com.devit.mscore.Configuration;
import com.devit.mscore.Schema;
import com.devit.mscore.Schema.Index;
import com.devit.mscore.exception.ApplicationRuntimeException;
import com.devit.mscore.exception.ConfigException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.bson.Document;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;

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
    repositories = new HashMap<>();
  }

  public static MongoDatabaseFactory of(Configuration configuration) {
    return new MongoDatabaseFactory(configuration);
  }

  public MongoRepository repository(Schema schema) {
    var domain = schema.getDomain();
    repositories.computeIfAbsent(domain,
        key -> {
          var collection = collection(domain);
          var uniqueAttributes = schema.getIndeces();
          createIndex(collection, uniqueAttributes);
          return new MongoRepository(collection);
        });
    return repositories.get(domain);
  }

  private static void createIndex(MongoCollection<Document> collection, List<Index> indeces) {
    var missingIndeces = getMissingIndex(collection, indeces);
    if (!missingIndeces.isEmpty()) {
      createMissingIndex(collection, missingIndeces);
    }
  }

  private static List<Index> getMissingIndex(MongoCollection<Document> collection, List<Index> indeces) {
    var nonExistingIndex = new ArrayList<Index>(indeces);
    for (var index : collection.listIndexes()) {
      var indexName = index.get("name").toString();
      nonExistingIndex.remove(new Index(indexName));
    }
    return nonExistingIndex;
  }

  private static void createMissingIndex(MongoCollection<Document> collection, List<Index> missingIndeces) {
    missingIndeces.forEach(index -> {
      var indexOptions = new IndexOptions().unique(index.isUnique());
      collection.createIndex(Indexes.ascending(index.getField()), indexOptions);
    });
  }

  public MongoCollection<Document> collection(String collection) {
    try {
      return database().getCollection(collection);
    } catch (ConfigException ex) {
      throw new ApplicationRuntimeException(ex);
    }
  }

  public MongoDatabase database() throws ConfigException {
    if (mongoDatabase == null) {
      var databaseName = getDatabaseName();
      mongoDatabase = mongoClient().getDatabase(databaseName);
    }
    return mongoDatabase;
  }

  private String getDatabaseName() throws ConfigException {
    return getConfig(DATABASE).orElseThrow(() -> new ConfigException("Mongo database is not configured"));
  }

  public MongoClient mongoClient() throws ConfigException {
    if (client != null) {
      return client;
    }

    var databaseName = getDatabaseName();
    var settingsBuilder = MongoClientSettings.builder();
    settingsBuilder.applyConnectionString(createConnectionString());

    var isSecure = getIsSecure();
    if (Boolean.parseBoolean(isSecure)) {
      settingsBuilder.credential(getCredential(databaseName));
    }

    return client = MongoClients.create(settingsBuilder.build());
  }

  protected ConnectionString createConnectionString() throws ConfigException {
    var hostname = getHost();
    var portNum = getPort();
    return new ConnectionString(String.format("mongodb://%s:%s", hostname, portNum));
  }

  private String getHost() throws ConfigException {
    return getConfig(HOST).orElseThrow(() -> new ConfigException("Mongo host is not configured"));
  }

  private String getPort() throws ConfigException {
    return getConfig(PORT).orElseThrow(() -> new ConfigException("Mongo port is not configured"));
  }

  private String getIsSecure() throws ConfigException {
    return getConfig(SECURE).orElse("false");
  }

  protected MongoCredential getCredential(String databaseName)
      throws ConfigException {
    var username = getUsername();
    var password = getPassword();
    return MongoCredential.createCredential(username, databaseName, password.toCharArray());
  }

  private String getUsername() throws ConfigException {
    return getConfig(USERNAME).orElseThrow(() -> new ConfigException("Mongo username is not provided"));
  }

  private String getPassword() throws ConfigException {
    return getConfig(PASSWORD).orElseThrow(() -> new ConfigException("Mongo password is not provided"));
  }

  private Optional<String> getConfig(String key) throws ConfigException {
    var configName = String.format(CONFIG_TEMPLATE, key);
    return configuration.getConfig(configName);
  }
}
