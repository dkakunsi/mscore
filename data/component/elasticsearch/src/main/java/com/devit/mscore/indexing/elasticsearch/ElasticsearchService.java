package com.devit.mscore.indexing.elasticsearch;

import static com.devit.mscore.util.AttributeConstants.getId;

import com.devit.mscore.Logger;
import com.devit.mscore.exception.IndexingException;
import com.devit.mscore.logging.ApplicationLogger;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.PutMappingRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Index implementation using elasticsearch.
 *
 * @author dkakunsi
 */
public class ElasticsearchService {

  private static final Logger LOG = new ApplicationLogger(ElasticsearchService.class);

  private RestHighLevelClient client;

  ElasticsearchService(Supplier<RestHighLevelClient> clientSupplier) {
    this.client = clientSupplier.get();
  }

  void createIndex(Map<String, String> map) {
    map.forEach(this::buildIndex);
  }

  @SuppressWarnings("PMD.GuardLogStatement")
  void buildIndex(String indexName, String mapping) {
    LOG.info("Building index {}.", indexName);

    try {
      if (indexExists(indexName)) {
        LOG.trace("Index {} is exist. Trying to update it.", indexName);
        updateIndex(indexName, mapping);
      } else {
        LOG.trace("Index {} is not exist. Trying to create it.", indexName);
        createIndex(indexName, mapping);
      }
    } catch (IndexingException ex) {
      LOG.error("Cannot build index: {}. Reason: {}", ex, indexName, ex.getMessage());
    }
  }

  private boolean indexExists(String indexName) throws IndexingException {
    var request = new GetIndexRequest(indexName);
    try {
      return client.indices().exists(request, RequestOptions.DEFAULT);
    } catch (IOException ex) {
      LOG.error("Fail checking index {}.", ex, indexName);
      throw new IndexingException("Cannot check index mapping: " + indexName, ex);
    }
  }

  private void createIndex(String indexName, String mapping) throws IndexingException {
    var request = new CreateIndexRequest(indexName);
    request.mapping(mapping, XContentType.JSON);

    try {
      var response = this.client.indices().create(request, RequestOptions.DEFAULT);

      if (response.isAcknowledged()) {
        LOG.info("Index mapping {} is created.", indexName);
      } else {
        LOG.error("Fail creating index {}.", indexName);
        throw new IndexingException("Create index mapping is not acknowledge: " + indexName);
      }
    } catch (IOException ex) {
      LOG.error("Fail creating index {}.", ex, indexName);
      throw new IndexingException("Cannot create index mapping: " + indexName, ex);
    }
  }

  private void updateIndex(String indexName, String mapping) throws IndexingException {
    var request = new PutMappingRequest(indexName);
    request.source(mapping, XContentType.JSON);

    try {
      var putMappingResponse = client.indices().putMapping(request, RequestOptions.DEFAULT);

      if (putMappingResponse.isAcknowledged()) {
        LOG.info("Index mapping {} is updated.", indexName);
      } else {
        LOG.error("Fail updating index {}.", indexName);
        throw new IndexingException("Update index mapping is not acknowledge: " + indexName);
      }
    } catch (IOException ex) {
      LOG.error("Fail updating index {}.", ex, indexName);
      throw new IndexingException("Cannot update index mapping: " + indexName, ex);
    }
  }

  String index(String indexName, JSONObject json) throws IndexingException {
    LOG.info("Indexing document to {} domain: {}", indexName, json);

    var id = getId(json);

    try {
      if (exists(indexName, id)) {
        return update(indexName, json, id);
      }
      return insert(indexName, json, id);
    } finally {
      LOG.debug("Document {} is indexed.", id);
    }
  }

  private boolean exists(String indexName, String id) throws IndexingException {
    return get(indexName, id).isPresent();
  }

  private String update(String indexName, JSONObject object, String id) throws IndexingException {
    LOG.debug("Updating document in {}: {}", indexName, object);

    var request = new UpdateRequest(indexName, object.toString()).id(id);
    request.doc(object.toString(), XContentType.JSON);

    try {
      var response = this.client.update(request, RequestOptions.DEFAULT);
      return response.getId();
    } catch (IOException ex) {
      LOG.error("Fail updating document in index {}.", ex, indexName);
      throw new IndexingException("Failed updating document in index: " + indexName, ex);
    }
  }

  private String insert(String indexName, JSONObject object, String id) throws IndexingException {
    LOG.debug("Indexing document into {}: {}", indexName, object);

    var request = new IndexRequest(indexName).id(id);
    request.source(object.toString(), XContentType.JSON);

    try {
      var response = this.client.index(request, RequestOptions.DEFAULT);
      return response.getId();
    } catch (IOException ex) {
      LOG.error("Fail indexing document in index {}.", ex, indexName);
      throw new IndexingException("Failed indexing document in index: " + indexName, ex);
    }
  }

  Optional<JSONObject> get(String indexName, String id) throws IndexingException {
    LOG.debug("Loading indexed document in {} with id: {}", indexName, id);

    var str = "{\"criteria\":[{\"attribute\":\"id\",\"operator\":\"equals\",\"value\":\"" + id
        + "\"}],\"page\":0,\"size\":1}";
    var query = new JSONObject(str);

    var searchRequest = new SearchRequest(indexName);
    searchRequest.searchType(SearchType.DFS_QUERY_THEN_FETCH);
    searchRequest.source(new SearchSourceBuilder().postFilter(ElasticsearchQueryHelper.createQuery(query)));

    var array = search(indexName, query);
    if (array.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(array.get().getJSONObject(0));
  }

  Optional<JSONArray> search(String indexName, JSONObject query) throws IndexingException {
    var queryBuilder = ElasticsearchQueryHelper.createQuery(query);
    LOG.debug("Executing: {}", queryBuilder);

    var queryPage = query.getInt("page");
    var querySize = query.getInt("size");
    SearchSourceBuilder ssb = new SearchSourceBuilder();
    ssb.postFilter(queryBuilder);
    ssb.from(queryPage * querySize);
    ssb.size(querySize);

    var searchRequest = new SearchRequest(indexName);
    searchRequest.searchType(SearchType.DFS_QUERY_THEN_FETCH);
    searchRequest.source(ssb);

    try {
      LOG.debug("Searching index: {}", indexName);
      var response = this.client.search(searchRequest, RequestOptions.DEFAULT);
      return extractResponse(response);
    } catch (IOException ex) {
      LOG.error("Fail searching document in index {}.", ex, indexName);
      throw new IndexingException("Fail to search index.", ex);
    }
  }

  private Optional<JSONArray> extractResponse(SearchResponse response) {
    var hits = response.getHits().getHits();
    if (hits.length <= 0) {
      return Optional.empty();
    }

    var array = new JSONArray();
    for (var hit : hits) {
      array.put(new JSONObject(hit.getSourceAsMap()));
    }

    LOG.debug("Returning hit document: {}", array);
    return Optional.of(array);
  }
}
