package com.devit.mscore.db.mongo;

import static com.devit.mscore.util.AttributeConstants.getId;

import com.devit.mscore.Index;
import com.devit.mscore.Repository;
import com.devit.mscore.exception.DataException;
import com.devit.mscore.exception.IndexingException;

import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONObject;

public class MongoIndex extends Index {

  private Repository repository;

  protected MongoIndex(String indexName, Repository repository) {
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
      if (!criteria.isEqualitySearch()) {
        logger.error("Search operator '{}' is not supported", criteria.getOperator());
        throw new IndexingException("Search operator currently not supported");
      }
      return repository.find(criteria.getAttribute(), criteria.getValue());
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
