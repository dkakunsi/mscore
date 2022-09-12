package com.devit.mscore.db.mongo;

import static com.devit.mscore.util.AttributeConstants.ID;
import static com.devit.mscore.util.AttributeConstants.getCode;
import static com.devit.mscore.util.AttributeConstants.getId;
import static com.devit.mscore.util.AttributeConstants.hasId;
import static com.devit.mscore.util.JsonUtils.copy;

import com.devit.mscore.Logger;
import com.devit.mscore.Repository;
import com.devit.mscore.exception.DataDuplicationException;
import com.devit.mscore.exception.DataException;
import com.devit.mscore.logging.ApplicationLogger;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONObject;

import com.mongodb.DuplicateKeyException;
import com.mongodb.MongoWriteException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;

/**
 * Root class of mongo repository implementation.
 *
 * @author dkakunsi
 */
public class MongoRepository implements Repository {

  protected static final Logger LOG = ApplicationLogger.getLogger(MongoRepository.class);

  protected static final String KEY_NOT_SUPPLIED = "The key is not supplied.";

  private static final String MONGO_ID = "_id";

  private static final String DUPLICATE_MESSAGE = "E11000 duplicate key error";

  protected MongoCollection<Document> collection;

  MongoRepository(MongoCollection<Document> collection) {
    super();
    this.collection = collection;
  }

  @Override
  @SuppressWarnings("PMD.GuardLogStatement")
  public JSONObject save(JSONObject json) throws DataException {
    try {
      LOG.info("Saving entity to MongoDB: {}", getCode(json));

      var id = getOrCreateId(json);
      var target = find(id, false).orElse(new JSONObject());
      copy(target, json);

      var document = toDocument(target);
      target.remove(MONGO_ID);

      /*
       * Upsert = true. When trying to add dependency with ID, it will be persisted in
       * DB.
       */
      var options = new ReplaceOptions().upsert(true);
      var filter = new Document(ID, id);
      this.collection.replaceOne(filter, document, options);

      return target;
    } catch (DuplicateKeyException ex) {
      throw new DataDuplicationException("Key is duplicated", ex);
    } catch (MongoWriteException ex) {
      if (ex.getMessage().contains(DUPLICATE_MESSAGE)) {
        throw new DataDuplicationException("Key is duplicated", ex);
      } else {
        throw new DataException(ex);
      }
    } catch (Exception ex) {
      throw new DataException(ex);
    }
  }

  private String getOrCreateId(JSONObject json) {
    if (!hasId(json)) {
      var id = UUID.randomUUID().toString();
      json.put(ID, id);
    }
    return getId(json);
  }

  private Document toDocument(JSONObject source) {
    var document = new Document(source.toMap());
    if (source.has(MONGO_ID)) {
      document.put(MONGO_ID, new ObjectId(source.getString(MONGO_ID)));
    }
    return document;
  }

  @Override
  public void delete(String id) {
    LOG.debug("Deleting entity from MongoDB: {}", id);
    this.collection.deleteOne(new Document(ID, id));
  }

  @Override
  public Optional<JSONObject> find(String id) {
    return find(id, true);
  }

  public Optional<JSONObject> find(String id, boolean removeMongoId) {
    LOG.trace("Finding data {}", id);
    var result = this.collection.find(new Document(ID, id));
    var document = result.first();
    if (document == null) {
      return Optional.empty();
    }
    return Optional.of(toJson(document, removeMongoId));
  }

  @Override
  public Optional<JSONArray> find(String field, Object value) {
    var filter = Filters.eq(field, value);
    var result = this.collection.find(filter);
    return loadResult(result);
  }

  @Override
  public Optional<JSONArray> find(List<String> keys) {
    var filter = Filters.in(ID, keys);
    var result = this.collection.find(filter);
    return loadResult(result);
  }

  @Override
  public Optional<JSONArray> all() {
    var result = this.collection.find();
    return loadResult(result);
  }

  private Optional<JSONArray> loadResult(FindIterable<Document> result) {
    if (!result.iterator().hasNext()) {
      return Optional.empty();
    }

    var jsons = new JSONArray();
    result.forEach((Consumer<Document>) document -> jsons.put(toJson(document, true)));

    return Optional.of(jsons);
  }

  private JSONObject toJson(Document document, boolean removeMongoId) {
    if (removeMongoId) {
      document.remove(MONGO_ID);
    } else {
      var mongoId = document.get(MONGO_ID).toString();
      document.put(MONGO_ID, mongoId);
    }
    return new JSONObject(document.toJson());
  }

  @Override
  public Object clone() throws CloneNotSupportedException {
    return super.clone();
  }
}
