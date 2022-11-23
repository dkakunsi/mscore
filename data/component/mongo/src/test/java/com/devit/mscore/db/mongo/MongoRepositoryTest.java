package com.devit.mscore.db.mongo;

import static com.devit.mscore.util.AttributeConstants.getId;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.devit.mscore.Configuration;
import com.devit.mscore.Schema;
import com.devit.mscore.exception.ConfigException;
import com.devit.mscore.exception.DataDuplicationException;
import com.devit.mscore.exception.DataException;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.mongodb.BasicDBObject;
import com.mongodb.DuplicateKeyException;
import com.mongodb.MongoClient;
import com.mongodb.MongoWriteException;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;

public class MongoRepositoryTest {

  private static final Integer RUNNING_PORT = 12345;

  private static final String DATABASE_NAME = "test";

  private static final String USERNAME = "username";

  private static final String PASSWORD = "****";

  private MongoRepository repository;

  private static MongodExecutable mongodExecutable;

  private static MongodProcess mongod;

  @BeforeClass
  public static void setupServer() throws IOException {
    var starter = MongodStarter.getDefaultInstance();
    var mongodConfig = MongodConfig.builder()
        .version(Version.Main.PRODUCTION)
        .net(new Net(RUNNING_PORT, Network.localhostIsIPv6()))
        .build();

    mongodExecutable = starter.prepare(mongodConfig);
    mongod = mongodExecutable.start();

    createUser();
  }

  @AfterClass
  public static void stopServer() {
    mongod.stop();
    mongodExecutable.stop();
  }

  private static void createUser() {
    try (var mongo = new MongoClient("localhost", RUNNING_PORT)) {
      var db = mongo.getDatabase(DATABASE_NAME);
      var roles = new BasicDBObject("role", "dbOwner")
          .append("db", DATABASE_NAME);
      var command = new BasicDBObject("createUser", USERNAME)
          .append("pwd", PASSWORD)
          .append("roles", Collections.singletonList(roles));
      db.runCommand(command);
    }
  }

  @Before
  public void setup() throws ConfigException {
    var configuration = mock(Configuration.class);
    doReturn(Optional.of("localhost")).when(configuration).getConfig("platform.mongo.host");
    doReturn(Optional.of(RUNNING_PORT.toString())).when((configuration)).getConfig("platform.mongo.port");
    doReturn(Optional.of(DATABASE_NAME)).when((configuration)).getConfig("platform.mongo.database");
    doReturn(Optional.of("true")).when((configuration)).getConfig("platform.mongo.secure");
    doReturn(Optional.of(USERNAME)).when(configuration).getConfig("platform.mongo.username");
    doReturn(Optional.of(PASSWORD)).when(configuration).getConfig("platform.mongo.password");

    var databaseFactory = MongoDatabaseFactory.of(configuration);

    var schema = mock(Schema.class);
    doReturn("domain").when(schema).getDomain();
    doReturn(List.of("id", "code")).when(schema).getUniqueAttributes();

    this.repository = databaseFactory.repository(schema);
  }

  @Test
  public void testSave() throws DataException, JSONException {
    var json = new JSONObject();
    json.put("id", "999999999999999999999001");
    json.put("name", "name1");
    json.put("domain", "domain");
    json.put("code", "code1");

    var result = this.repository.save(json);
    assertTrue(result.similar(json));

    // find by id
    var optResult = this.repository.find("999999999999999999999001");
    assertTrue(optResult.isPresent());
    result = optResult.get();
    assertTrue(result.similar(json));

    // find by code
    var optResultArr = this.repository.findByCriteria(List.of(Pair.of("code", "code1")));
    assertTrue(optResultArr.isPresent());
    var resultArr = optResultArr.get();
    result = resultArr.getJSONObject(0);
    assertTrue(result.similar(json));

    // find by code and domain
    optResultArr = this.repository.findByCriteria(List.of(Pair.of("code", "code1"), Pair.of("domain", "domain")));
    assertTrue(optResultArr.isPresent());
    resultArr = optResultArr.get();
    result = resultArr.getJSONObject(0);
    assertTrue(result.similar(json));
  }

  @Test
  public void testSave_Create() throws DataException, JSONException {
    var json = new JSONObject();
    json.put("domain", "domain");
    json.put("name", "name2");
    json.put("code", "code2");
    var result = this.repository.save(json);

    assertNotNull(result);
    assertTrue(StringUtils.isNotBlank(getId(result)));
  }

  @Test
  public void testSave_DuplicatedKey() throws DataException, JSONException {
    var json = new JSONObject();
    json.put("id", "999999999999999999999003");
    json.put("domain", "domain");
    json.put("name", "name3");
    json.put("code", "code3");
    this.repository.save(json);

    var json2 = new JSONObject();
    json2.put("domain", "domain");
    json2.put("name", "name");
    json2.put("code", "code3");

    var ex = assertThrows(DataDuplicationException.class, () -> this.repository.save(json2));
    assertThat(ex.getMessage(), is("Key is duplicated"));
    assertTrue(ex.getCause() instanceof DuplicateKeyException || ex.getCause() instanceof MongoWriteException);
  }

  @Test
  public void testDelete() throws DataException {
    var json = new JSONObject();
    json.put("id", "999999999999999999999004");
    json.put("domain", "domain");
    json.put("name", "name4");
    json.put("code", "code4");
    this.repository.save(json);
    this.repository.delete("999999999999999999999004");
    var result = this.repository.find("999999999999999999999004");
    assertTrue(result.isEmpty());
  }

  @Test
  public void testFindKeys() throws DataException {
    var json1 = new JSONObject();
    json1.put("id", "999999999999999999999005");
    json1.put("domain", "domain");
    json1.put("name", "name5");
    json1.put("code", "code5");
    this.repository.save(json1);

    var json2 = new JSONObject();
    json2.put("id", "999999999999999999999006");
    json2.put("domain", "domain");
    json2.put("name", "name6");
    json2.put("code", "code6");
    this.repository.save(json2);
    var resultOpt = this.repository.find(List.of("999999999999999999999005", "999999999999999999999006"));
    assertTrue(resultOpt.isPresent());
    var result = resultOpt.get();
    assertThat(result.length(), is(2));
  }

  @Test
  public void testAll() throws DataException {
    var json1 = new JSONObject();
    json1.put("id", "999999999999999999999007");
    json1.put("domain", "domain");
    json1.put("name", "name7");
    json1.put("code", "code7");
    this.repository.save(json1);

    var json2 = new JSONObject();
    json2.put("id", "999999999999999999999008");
    json2.put("domain", "domain");
    json2.put("name", "name8");
    json2.put("code", "code8");
    this.repository.save(json2);
    var resultOpt = this.repository.all();
    assertTrue(resultOpt.isPresent());
    var result = resultOpt.get();
    assertThat(result.length(), is(greaterThan(1)));
  }

  @Test
  public void testFindKeys_NotFound() throws DataException {
    var resultOpt = this.repository.find(List.of("999999999999999999999009"));
    assertTrue(resultOpt.isEmpty());
  }

  @Test
  public void testGetById_WithMongoId() throws DataException {
    var json = new JSONObject();
    json.put("id", "999999999999999999999010");
    json.put("domain", "domain");
    json.put("name", "name10");
    json.put("code", "code10");
    this.repository.save(json);
    var resultOpt = this.repository.find("999999999999999999999010", false);
    assertTrue(resultOpt.isPresent());
    var result = resultOpt.get();
    assertTrue(StringUtils.isNotBlank(result.getString("_id")));
  }
}
