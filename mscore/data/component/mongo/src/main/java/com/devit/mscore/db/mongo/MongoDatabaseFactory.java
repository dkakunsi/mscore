package com.devit.mscore.db.mongo;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.devit.mscore.ApplicationContext;
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

    public MongoRepository repository(ApplicationContext context, Schema schema) {
        var domain = schema.getDomain();
        this.repositories.computeIfAbsent(domain, key -> new MongoRepository(collection(context, domain), schema.getUniqueAttributes()));
        return this.repositories.get(domain);
    }

    public MongoCollection<Document> collection(ApplicationContext context, String collection) {
        try {
            return database(context).getCollection(collection);
        } catch (ConfigException ex) {
            throw new ApplicationRuntimeException(ex);
        }
    }

    public MongoDatabase database(ApplicationContext context) throws ConfigException {
        if (this.mongoDatabase == null) {
            var databaseName = getDatabaseName(context);
            this.mongoDatabase = mongoClient(context).getDatabase(databaseName);
        }
        return this.mongoDatabase;
    }

    public MongoClient mongoClient(ApplicationContext context) throws ConfigException {
        if (this.client != null) {
            return this.client;
        }

        var databaseName = getDatabaseName(context);
        var settingsBuilder = MongoClientSettings.builder();
        applyAuthentication(context, settingsBuilder, databaseName, createConnectionString(context));
        this.client = MongoClients.create(settingsBuilder.build());
        return this.client;
    }

    private String getDatabaseName(ApplicationContext context) throws ConfigException {
        return getConfig(context, DATABASE).orElseThrow(() -> new ConfigException("Mongo database is not configured."));
    }

    protected ConnectionString createConnectionString(ApplicationContext context) throws ConfigException {
        var hostname = getHost(context);
        var portNum = getPort(context);
        return new ConnectionString(String.format("mongodb://%s:%s", hostname, portNum));
    }

    private String getHost(ApplicationContext context) throws ConfigException {
        return getConfig(context, HOST).orElseThrow(() -> new ConfigException("Mongo host is not configured."));
    }

    private String getPort(ApplicationContext context) throws ConfigException {
        return getConfig(context, PORT).orElseThrow(() -> new ConfigException("Mongo port is not configured."));
    }

    protected void applyAuthentication(ApplicationContext context, Builder builder, String databaseName, ConnectionString connectionString) throws ConfigException {
        var isSecure = getIsSecure(context);
        if (Boolean.parseBoolean(isSecure)) {
            var username = getUsername(context);
            var password = getPassword(context);
            var credential = MongoCredential.createCredential(username, databaseName, password.toCharArray());
            builder.applyConnectionString(connectionString).credential(credential);
        }
    }

    private String getIsSecure(ApplicationContext context) throws ConfigException {
        return getConfig(context, SECURE).orElse("false");
    }

    private String getUsername(ApplicationContext context) throws ConfigException {
        return getConfig(context, USERNAME).orElseThrow(() -> new ConfigException("Mongo username is not provided."));
    }

    private String getPassword(ApplicationContext context) throws ConfigException {
        return getConfig(context, PASSWORD).orElseThrow(() -> new ConfigException("Mongo password is not provided."));
    }

    private Optional<String> getConfig(ApplicationContext context, String key) throws ConfigException {
        var configName = String.format(CONFIG_TEMPLATE, key);
        return this.configuration.getConfig(context, configName);
    }

    // TODO remove this after implementing embedded-mongo for testing.
    public void setMongoClient(MongoClient mongoClient) {
        this.client = mongoClient;
    }
}
