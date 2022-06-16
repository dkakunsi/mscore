package com.devit.mscore.db.mongo;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Optional;

import com.devit.mscore.ApplicationContext;
import com.devit.mscore.Configuration;
import com.devit.mscore.DefaultApplicationContext;
import com.devit.mscore.Schema;
import com.devit.mscore.exception.ConfigException;
import com.mongodb.ConnectionString;
import com.mongodb.MongoCredential;
import com.mongodb.MongoClientSettings.Builder;
import com.mongodb.client.ListIndexesIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

import org.bson.Document;
import org.junit.Before;
import org.junit.Test;

public class MongoDatabaseFactoryTest {

    private MongoDatabaseFactory factory;

    private Configuration configuration;

    private MongoClient client;

    private ApplicationContext context;

    @Before
    public void setup() {
        this.configuration = mock(Configuration.class);
        this.client = mock(MongoClient.class);
        this.factory = MongoDatabaseFactory.of(configuration);
        this.factory.setMongoClient(this.client);
        this.context = DefaultApplicationContext.of("test");
    }

    @Test
    public void testGetRepository() throws ConfigException {
        var schema = mock(Schema.class);
        doReturn("domain").when(schema).getDomain();
        doReturn(List.of("id")).when(schema).getUniqueAttributes();

        var document = mock(Document.class);
        doReturn("_id").when(document).get(eq("name"));
        var mongoCursor = mock(MongoCursor.class);
        doReturn(document).when(mongoCursor).next();
        doReturn(true, false).when(mongoCursor).hasNext();
        var listIndeces = mock(ListIndexesIterable.class);
        doReturn(mongoCursor).when(listIndeces).iterator();
        var collection = mock(MongoCollection.class);
        doReturn(listIndeces).when(collection).listIndexes();
        var database = mock(MongoDatabase.class);
        doReturn(collection).when(database).getCollection("domain");

        doReturn(database).when(this.client).getDatabase("database");
        doReturn(Optional.of("database")).when(this.configuration).getConfig(any(ApplicationContext.class), eq("platform.mongo.database"));

        var repository = this.factory.repository(this.context, schema);
        assertNotNull(repository);
    }

    @Test
    public void testCreateConnectionString() throws ConfigException {
        doReturn(Optional.of("mongo")).when(this.configuration).getConfig(any(ApplicationContext.class), eq("platform.mongo.host"));
        doReturn(Optional.of("1000")).when(this.configuration).getConfig(any(ApplicationContext.class), eq("platform.mongo.port"));

        var connectionString = this.factory.createConnectionString(context);
        assertNotNull(connectionString);
    }

    @Test
    public void testApplyAuthentication() throws ConfigException {
        doReturn(Optional.of("true")).when(this.configuration).getConfig(any(ApplicationContext.class), eq("platform.mongo.secure"));
        doReturn(Optional.of("username")).when(this.configuration).getConfig(any(ApplicationContext.class), eq("platform.mongo.username"));
        doReturn(Optional.of("password")).when(this.configuration).getConfig(any(ApplicationContext.class), eq("platform.mongo.password"));

        var builder = mock(Builder.class);
        var connectionString = mock(ConnectionString.class);
        doReturn(builder).when(builder).applyConnectionString(connectionString);

        this.factory.applyAuthentication(this.context, builder, "databaseName", connectionString);
        verify(builder, times(1)).credential(any(MongoCredential.class));
    }
}
