package com.devit.mscore.indexing.elasticsearch;

import static com.devit.mscore.util.AttributeConstants.getId;

import com.devit.mscore.Logger;
import com.devit.mscore.exception.IndexingException;
import com.devit.mscore.logging.ApplicationLogger;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

  private static final Logger LOGGER = ApplicationLogger.getLogger(ElasticsearchService.class);

  private RestHighLevelClient client;

  ElasticsearchService(Supplier<RestHighLevelClient> clientSupplier) {
    this.client = clientSupplier.get();
  }

  void createIndex(Map<String, String> map) {
    map.forEach(this::buildIndex);
  }

  void buildIndex(String indexName, String mapping) {
    LOGGER.info("Building index '{}'", indexName);

    try {
      if (indexExists(indexName)) {
        LOGGER.trace("Updating index '{}'", indexName);
        updateIndex(indexName, mapping);
      } else {
        LOGGER.trace("Creating index '{}'", indexName);
        createIndex(indexName, mapping);
      }
    } catch (IndexingException ex) {
      LOGGER.error("Cannot build index '{}'. Reason: {}", ex, indexName, ex.getMessage());
    }
  }

  private boolean indexExists(String indexName) throws IndexingException {
    var request = new GetIndexRequest(indexName);
    try {
      return client.indices().exists(request, RequestOptions.DEFAULT);
    } catch (IOException ex) {
      LOGGER.error("Fail checking index '{}'", ex, indexName);
      throw new IndexingException("Cannot check index mapping: " + indexName, ex);
    }
  }

  private void createIndex(String indexName, String mapping) throws IndexingException {
    var request = new CreateIndexRequest(indexName);
    request.mapping(mapping, XContentType.JSON);

    try {
      var response = client.indices().create(request, RequestOptions.DEFAULT);

      if (response.isAcknowledged()) {
        LOGGER.info("Index '{}' is created", indexName);
      } else {
        LOGGER.error("Fail creating index '{}'", indexName);
        throw new IndexingException("Create index is not acknowledge for " + indexName);
      }
    } catch (IOException ex) {
      LOGGER.error("Fail creating index '{}'", ex, indexName);
      throw new IndexingException("Cannot create index " + indexName, ex);
    }
  }

  private void updateIndex(String indexName, String mapping) throws IndexingException {
    var request = new PutMappingRequest(indexName);
    request.source(mapping, XContentType.JSON);

    try {
      var putMappingResponse = client.indices().putMapping(request, RequestOptions.DEFAULT);

      if (putMappingResponse.isAcknowledged()) {
        LOGGER.info("Index '{}' is updated", indexName);
      } else {
        LOGGER.error("Fail to update index '{}'", indexName);
        throw new IndexingException("Update is not acknowledge for index: " + indexName);
      }
    } catch (IOException ex) {
      LOGGER.error("Fail updating index '{}'", ex, indexName);
      throw new IndexingException("Cannot update index: " + indexName, ex);
    }
  }

  String index(String indexName, JSONObject json) throws IndexingException {
    var id = getId(json);
    LOGGER.info("Indexing document '{}' to index '{}'", id, indexName);
    var result = exists(indexName, id) ? update(indexName, json, id) : insert(indexName, json, id);
    LOGGER.info("Document '{}' is indexed to '{}'", id, indexName);
    return result;
  }

  private boolean exists(String indexName, String id) throws IndexingException {
    return get(indexName, id).isPresent();
  }

  private String update(String indexName, JSONObject object, String id) throws IndexingException {
    LOGGER.debug("Updating document '{}' in index '{}'", id, indexName);

    var request = new UpdateRequest(indexName, object.toString()).id(id);
    request.doc(object.toString(), XContentType.JSON);

    try {
      var response = client.update(request, RequestOptions.DEFAULT);
      return response.getId();
    } catch (IOException ex) {
      LOGGER.error("Fail to update document '{}' in index '{}'", ex, id, indexName);
      throw new IndexingException("Failed to update document in index: " + indexName, ex);
    }
  }

  private String insert(String indexName, JSONObject object, String id) throws IndexingException {
    LOGGER.debug("Indexing document '{}' into index '{}'", id, indexName);

    var request = new IndexRequest(indexName).id(id);
    request.source(object.toString(), XContentType.JSON);

    try {
      var response = client.index(request, RequestOptions.DEFAULT);
      return response.getId();
    } catch (IOException ex) {
      LOGGER.error("Fail to insert document '{}' in index '{}'", ex, id, indexName);
      throw new IndexingException("Failed to insert document in index: " + indexName, ex);
    }
  }

  Optional<JSONObject> get(String indexName, String id) throws IndexingException {
    LOGGER.debug("Retrieving document '{}' from index '{}'", id, indexName);

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
    LOGGER.debug("Executing search query: {}", queryBuilder);

    var queryPage = query.has("page") ? query.optInt("page") : 0;
    var querySize = query.has("size") ? query.getInt("size") : 10000;
    SearchSourceBuilder ssb = new SearchSourceBuilder();
    ssb.postFilter(queryBuilder);
    ssb.from(queryPage * querySize);
    ssb.size(querySize);

    var searchRequest = new SearchRequest(indexName);
    searchRequest.searchType(SearchType.DFS_QUERY_THEN_FETCH);
    searchRequest.source(ssb);

    try {
      var response = client.search(searchRequest, RequestOptions.DEFAULT);
      return extractResponse(response);
    } catch (IOException ex) {
      LOGGER.error("Fail to search document in index '{}'", ex, indexName);
      throw new IndexingException("Fail to search document in index", ex);
    }
  }

  private Optional<JSONArray> extractResponse(SearchResponse response) {
    var hits = response.getHits().getHits();
    if (hits.length <= 0) {
      return Optional.empty();
    }

    var list = Stream.of(hits)
        .map(h -> new JSONObject(h.getSourceAsMap()))
        .collect(Collectors.toList());

    LOGGER.debug("Returning hit document: {}", list);
    return Optional.of(new JSONArray(list));
  }
}
