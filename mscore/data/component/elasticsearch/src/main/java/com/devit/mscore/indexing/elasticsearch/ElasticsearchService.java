package com.devit.mscore.indexing.elasticsearch;

import static com.devit.mscore.util.AttributeConstants.getId;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import com.devit.mscore.ApplicationContext;
import com.devit.mscore.exception.IndexingException;

/**
 * Index implementation using elasticsearch.
 * 
 * @author dkakunsi
 */
public class ElasticsearchService {

    private static final Logger LOG = LoggerFactory.getLogger(ElasticsearchService.class);

    private RestHighLevelClient client;

    private boolean indexCreated;

    public ElasticsearchService(RestHighLevelClient client) {
        this.client = client;
        this.indexCreated = false;
    }

    void createIndex(ApplicationContext context, Map<String, String> map) {

        if (indexCreated) {
            return;
        }

        map.forEach((indexName, mapping) -> {
            buildIndex(context, indexName, mapping);
        });
    }

    void buildIndex(ApplicationContext context, String indexName, String mapping) {
        LOG.info("BreadcrumbdId: {}. Building index {}.", context.getBreadcrumbId(), indexName);

        try {
            if (indexExists(context, indexName)) {
                LOG.trace("BreadcrumbdId: {}. Index {} is exist. Trying to update it.", context.getBreadcrumbId(), indexName);
                updateIndex(context, indexName, mapping);
            } else {
                LOG.trace("BreadcrumbdId: {}. Index {} is not exist. Trying to create it.", context.getBreadcrumbId(), indexName);
                createIndex(context, indexName, mapping);
            }
        } catch (IndexingException ex) {
            LOG.error("BreadcrumbdId: {}. Cannot build index: {}. Reason: {}", context.getBreadcrumbId(), indexName, ex.getMessage(), ex);
        }
    }

    private boolean indexExists(ApplicationContext context, String indexName) throws IndexingException {
        var request = new GetIndexRequest(indexName);
        try {
            return client.indices().exists(request, RequestOptions.DEFAULT);
        } catch (IOException ex) {
            LOG.error("BreadcrumbId: {}. Fail checking index {}.", context.getBreadcrumbId(), indexName);
            throw new IndexingException("Cannot check index mapping: " + indexName, ex);
        }
    }

    private void createIndex(ApplicationContext context, String indexName, String mapping) throws IndexingException {
        var request = new CreateIndexRequest(indexName);
        request.mapping(mapping, XContentType.JSON);

        try {
            var response = this.client.indices().create(request, RequestOptions.DEFAULT);

            if (response.isAcknowledged()) {
                LOG.info("BreadcrumbId: {}. Index mapping {} is created.", context.getBreadcrumbId(), indexName);
            } else {
                LOG.error("BreadcrumbId: {}. Fail creating index {}.", context.getBreadcrumbId(), indexName);
                throw new IndexingException("Create index mapping is not acknowledge: " + indexName);
            }
        } catch (IOException ex) {
            LOG.error("BreadcrumbId: {}. Fail creating index {}.", context.getBreadcrumbId(), indexName);
            throw new IndexingException("Cannot create index mapping: " + indexName, ex);
        }
    }

    private void updateIndex(ApplicationContext context, String indexName, String mapping) throws IndexingException {
        var request = new PutMappingRequest(indexName);
        request.source(mapping, XContentType.JSON);

        try {
            var putMappingResponse = client.indices().putMapping(request, RequestOptions.DEFAULT);

            if (putMappingResponse.isAcknowledged()) {
                LOG.info("BreadcrumbId: {}. Index mapping {} is updated.", context.getBreadcrumbId(), indexName);
            } else {
                LOG.error("BreadcrumbId: {}. Fail updating index {}.", context.getBreadcrumbId(), indexName);
                throw new IndexingException("Update index mapping is not acknowledge: " + indexName);
            }
        } catch (IOException ex) {
            LOG.error("BreadcrumbId: {}. Fail updating index {}.", context.getBreadcrumbId(), indexName);
            throw new IndexingException("Cannot update index mapping: " + indexName, ex);
        }
    }

    String index(ApplicationContext context, String indexName, JSONObject json) throws IndexingException {
        LOG.info("BreadcrumbId: {}. Indexing document to {} domain: {}", context.getBreadcrumbId(), indexName, json);

        var id = getId(json);

        try {
            if (exists(context, indexName, id)) {
                return update(context, indexName, json, id);
            }
            return insert(context, indexName, json, id);
        } finally {
            LOG.debug("BreadcrumbId: {}. Document {} is indexed.", context.getBreadcrumbId(), id);
        }
    }

    private boolean exists(ApplicationContext context, String indexName, String id) throws IndexingException {
        return get(context, indexName, id).isPresent();
    }

    private String update(ApplicationContext context, String indexName, JSONObject object, String id) throws IndexingException {
        LOG.debug("BreadcrumbId: {}. Updating document in {}: {}", context.getBreadcrumbId(), indexName, object);

        var request = new UpdateRequest(indexName, object.toString()).id(id);
        request.doc(object.toString(), XContentType.JSON);

        try {
            var response = this.client.update(request, RequestOptions.DEFAULT);
            return response.getId();
        } catch (IOException ex) {
            LOG.error("BreadcrumbId: {}. Fail updating document in index {}.", context.getBreadcrumbId(), indexName);
            throw new IndexingException("Failed updating document in index: " + indexName, ex);
        }
    }

    private String insert(ApplicationContext context, String indexName, JSONObject object, String id) throws IndexingException {
        LOG.debug("BreadcrumbId: {}. Indexing document into {}: {}", context.getBreadcrumbId(), indexName, object);

        var request = new IndexRequest(indexName).id(id);
        request.source(object.toString(), XContentType.JSON);

        try {
            var response = this.client.index(request, RequestOptions.DEFAULT);
            return response.getId();
        } catch (IOException ex) {
            LOG.error("BreadcrumbId: {}. Fail indexing document in index {}.", context.getBreadcrumbId(), indexName);
            throw new IndexingException("Failed indexing document in index: " + indexName, ex);
        }
    }

    Optional<JSONObject> get(ApplicationContext context, String indexName, String id) throws IndexingException {
        LOG.debug("BreadcrumbId: {}. Loading indexed document in {} with id: {}", context.getBreadcrumbId(), indexName, id);

        var str = "{\"criteria\":[{\"attribute\":\"id\",\"operator\":\"equals\",\"value\":\"" + id + "\"}],\"page\":0,\"size\":1}";
        var query = new JSONObject(str);

        var searchRequest = new SearchRequest(indexName);
        searchRequest.searchType(SearchType.DFS_QUERY_THEN_FETCH);
        searchRequest.source(new SearchSourceBuilder().postFilter(ElasticsearchQueryHelper.createQuery(query)));

        var array = search(context, indexName, query);
        if (array.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(array.get().getJSONObject(0));
    }

    Optional<JSONArray> search(ApplicationContext context, String indexName, JSONObject query) throws IndexingException {
        var queryBuilder = ElasticsearchQueryHelper.createQuery(query);
        LOG.debug("BreadcrumbId: {}. Executing: {}", context.getBreadcrumbId(), queryBuilder);

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
            LOG.debug("BreadcrumbId: {}. Searching index: {}", context.getBreadcrumbId(), indexName);
            var response = this.client.search(searchRequest, RequestOptions.DEFAULT);
            return extractResponse(context, response);
        } catch (IOException ex) {
            LOG.error("BreadcrumbId: {}. Fail searching document in index {}.", context.getBreadcrumbId(), indexName);
            throw new IndexingException("Fail to search index.", ex);
        }
    }

    private Optional<JSONArray> extractResponse(ApplicationContext context, SearchResponse response) {
        var hits = response.getHits().getHits();
        if (hits.length <= 0) {
            return Optional.empty();
        }

        var array = new JSONArray();
        for (var hit : hits) {
            array.put(new JSONObject(hit.getSourceAsMap()));
        }

        LOG.debug("BreadcrumbId: {}. Returning hit document: {}", context.getBreadcrumbId(), array);
        return Optional.of(array);
    }
}
