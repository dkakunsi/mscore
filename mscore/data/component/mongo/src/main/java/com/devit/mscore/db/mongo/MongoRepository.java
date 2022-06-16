package com.devit.mscore.db.mongo;

import static com.devit.mscore.util.AttributeConstants.ID;
import static com.devit.mscore.util.AttributeConstants.hasId;
import static com.devit.mscore.util.AttributeConstants.getCode;
import static com.devit.mscore.util.AttributeConstants.getId;
import static com.devit.mscore.util.JsonUtils.copy;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import com.devit.mscore.ApplicationContext;
import com.devit.mscore.Repository;
import com.devit.mscore.exception.DataDuplicationException;
import com.devit.mscore.exception.DataException;
import com.mongodb.DuplicateKeyException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.ReplaceOptions;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Root class of mongo repository implementation.
 * 
 * @author dkakunsi
 */
public class MongoRepository implements Repository {

    protected final Logger LOG = LoggerFactory.getLogger(getClass());

    protected static final String KEY_NOT_SUPPLIED = "The key is not supplied.";

    private static final String MONGO_ID = "_id";

    protected MongoCollection<Document> collection;

    MongoRepository(MongoCollection<Document> collection, List<String> uniqueAttributes) {
        super();
        this.collection = collection;
        createIndex(collection, uniqueAttributes);
    }

    private void createIndex(MongoCollection<Document> collection, List<String> uniqueAttributes) {
        var nonExistIndeces = new ArrayList<>(uniqueAttributes);
        for (var index : collection.listIndexes()) {
            var indexName = index.get("name");
            if (nonExistIndeces.contains(indexName)) {
                nonExistIndeces.remove(indexName);
            }
        }

        if (!nonExistIndeces.isEmpty()) {
            LOG.info("Creating indeces for: {}", nonExistIndeces);
            var indexOptions = new IndexOptions().unique(true);
            nonExistIndeces.forEach(index -> this.collection.createIndex(Indexes.ascending(index), indexOptions));
        }
    }

    public void setCollection(MongoCollection<Document> collection) {
        this.collection = collection;
    }

    @Override
    public JSONObject save(ApplicationContext context, JSONObject json) throws DataException {
        this.LOG.trace("BreadcrumbId: {}. Saving entity to MongoDB: {}", context.getBreadcrumbId(), getCode(json));

        try {

            var id = getOrCreateId(json);
            var target = find(context, id, false).orElse(new JSONObject());
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
        } catch (Exception ex) {
            throw new DataException(ex.getMessage(), ex);
        }
    }

    private String getOrCreateId(JSONObject json) {
        if (!hasId(json)) {
            var id = UUID.randomUUID().toString();
            json.put(ID, id);
        }
        return getId(json);
    }

    private Document toDocument(JSONObject target) {
        var document = new Document(target.toMap());
        if (target.has(MONGO_ID)) {
            document.put(MONGO_ID, new ObjectId(target.getString(MONGO_ID)));
        }
        return document;
    }

    @Override
    public void delete(ApplicationContext context, String id) {
        this.LOG.debug("BreadcrumbId: {}. Deleting entity from MongoDB: {}", context.getBreadcrumbId(), id);
        this.collection.deleteOne(new Document(ID, id));
    }

    @Override
    public Optional<JSONObject> find(ApplicationContext context, String id) {
        return find(context, id, true);
    }

    public Optional<JSONObject> find(ApplicationContext context, String id, boolean removeMongoId) {
        LOG.trace("BreadcrumbId: {}. Finding data {}", context.getBreadcrumbId(), id);
        var result = this.collection.find(new Document(ID, id));
        var document = result.first();
        if (document == null) {
            return Optional.empty();
        }
        return Optional.of(toJson(document, removeMongoId));
    }

    @Override
    public Optional<JSONArray> find(ApplicationContext context, String field, Object value) {
        var result = this.collection.find(new Document(field, value));
        return loadResult(result);
    }

    @Override
    public Optional<JSONArray> find(ApplicationContext context, List<String> keys) {
        var query = new Document(ID, keys);
        var result = this.collection.find(query);
        return loadResult(result);
    }

    @Override
    public Optional<JSONArray> all(ApplicationContext context) {
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
}
