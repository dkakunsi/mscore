package com.devit.mscore.db.mongo;

import static com.devit.mscore.util.AttributeConstants.getId;

import com.devit.mscore.Index;
import com.devit.mscore.Repository;
import com.devit.mscore.exception.DataException;
import com.devit.mscore.exception.IndexingException;

import java.util.ArrayList;
import java.util.Optional;

import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONArray;
import org.json.JSONObject;

public class MongoIndex extends Index {

  private Repository repository;

  public MongoIndex(String indexName, Repository repository) {
    super(indexName);
    this.repository = repository;
  }

  @Override
  public String index(JSONObject json) throws IndexingException {
    try {
      var indexedDoc = repository.save(json);
      return getId(indexedDoc);
    } catch (DataException ex) {
      logger.error("Cannot index document", ex);
      throw new IndexingException(ex);
    }
  }

  @Override
  public Optional<JSONArray> search(SearchCriteria criteria) throws IndexingException {
    criteria.validate();
    try {
      var listCriteria = new ArrayList<Pair<String, Object>>();
      for (var c : criteria.getCriteria()) {
        if (!c.isEqualitySearch()) {
          logger.error("Search operator '{}' is not supported", c.getOperator());
          throw new IndexingException("Search operator currently not supported");
        }
        listCriteria.add(Pair.of(c.getAttribute(), c.getValue()));
      }
      return repository.findByCriteria(listCriteria);
    } catch (DataException ex) {
      logger.error("Cannot search document", ex);
      throw new IndexingException(ex);
    }
  }

  @Override
  public Optional<JSONObject> get(String id) throws IndexingException {
    try {
      return repository.find(id);
    } catch (DataException ex) {
      logger.error("Cannot find document '{}'", ex, id);
      throw new IndexingException(ex);
    }
  }
}
