package com.devit.mscore.indexing.elasticsearch;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThrows;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.Map;

import com.devit.mscore.exception.IndexingException;

import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.PutMappingRequest;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.json.JSONException;
import org.json.JSONObject;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.IndicesClient;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.junit.Before;
import org.junit.Test;

public class ElasticsearchServiceTest {

    private ElasticsearchService index;

    private RestHighLevelClient client;

    @Before
    public void setup() {
        this.client = mock(RestHighLevelClient.class);
        this.index = new ElasticsearchService(this.client);
    }

    @Test
    public void testCreateIndex_ThrowIOException() throws IOException {
        var indices = mock(IndicesClient.class);
        doThrow(new IOException()).when(indices).exists(any(GetIndexRequest.class), any(RequestOptions.class));
        doReturn(indices).when(this.client).indices();

        this.index.createIndex(Map.of("map1", "{\"attribute\":\"value\"}"));

        verify(indices, times(1)).exists(any(GetIndexRequest.class), any(RequestOptions.class));
        verify(indices, times(0)).putMapping(any(PutMappingRequest.class), any(RequestOptions.class));
        verify(indices, times(0)).create(any(CreateIndexRequest.class), any(RequestOptions.class));
    }

    @Test
    public void testCreateIndex_Create_Acknowledge() throws IOException {
        var indices = mock(IndicesClient.class);
        doReturn(false).when(indices).exists(any(GetIndexRequest.class), any(RequestOptions.class));
        doReturn(new CreateIndexResponse(true, false, null)).when(indices).create(any(CreateIndexRequest.class),
                any(RequestOptions.class));
        doReturn(indices).when(this.client).indices();

        this.index.createIndex(Map.of("map1", "{\"attribute\":\"value\"}"));

        verify(indices, times(1)).exists(any(GetIndexRequest.class), any(RequestOptions.class));
        verify(indices, times(0)).putMapping(any(PutMappingRequest.class), any(RequestOptions.class));
        verify(indices, times(1)).create(any(CreateIndexRequest.class), any(RequestOptions.class));
    }

    @Test
    public void testCreateIndex_Create_NotAcknowledge() throws IOException {
        var indices = mock(IndicesClient.class);
        doReturn(false).when(indices).exists(any(GetIndexRequest.class), any(RequestOptions.class));
        doReturn(new CreateIndexResponse(false, false, null)).when(indices).create(any(CreateIndexRequest.class),
                any(RequestOptions.class));
        doReturn(indices).when(this.client).indices();

        this.index.createIndex(Map.of("map1", "{\"attribute\":\"value\"}"));

        verify(indices, times(1)).exists(any(GetIndexRequest.class), any(RequestOptions.class));
        verify(indices, times(0)).putMapping(any(PutMappingRequest.class), any(RequestOptions.class));
        verify(indices, times(1)).create(any(CreateIndexRequest.class), any(RequestOptions.class));
    }

    @Test
    public void testCreateIndex_Create_ThrowIOException() throws IOException {
        var indices = mock(IndicesClient.class);
        doReturn(false).when(indices).exists(any(GetIndexRequest.class), any(RequestOptions.class));
        doThrow(new IOException()).when(indices).create(any(CreateIndexRequest.class), any(RequestOptions.class));
        doReturn(indices).when(this.client).indices();

        this.index.createIndex(Map.of("map1", "{\"attribute\":\"value\"}"));

        verify(indices, times(1)).exists(any(GetIndexRequest.class), any(RequestOptions.class));
        verify(indices, times(0)).putMapping(any(PutMappingRequest.class), any(RequestOptions.class));
        verify(indices, times(1)).create(any(CreateIndexRequest.class), any(RequestOptions.class));
    }

    @Test
    public void testCreateIndex_Update_Acknowledge() throws IOException {
        var indices = mock(IndicesClient.class);
        doReturn(true).when(indices).exists(any(GetIndexRequest.class), any(RequestOptions.class));
        doReturn(new AcknowledgedResponse(true)).when(indices).putMapping(any(PutMappingRequest.class),
                any(RequestOptions.class));
        doReturn(indices).when(this.client).indices();

        this.index.createIndex(Map.of("map1", "{\"attribute\":\"value\"}"));

        verify(indices, times(1)).exists(any(GetIndexRequest.class), any(RequestOptions.class));
        verify(indices, times(1)).putMapping(any(PutMappingRequest.class), any(RequestOptions.class));
        verify(indices, times(0)).create(any(CreateIndexRequest.class), any(RequestOptions.class));
    }

    @Test
    public void testCreateIndex_Update_NotAcknowledge() throws IOException {
        var indices = mock(IndicesClient.class);
        doReturn(true).when(indices).exists(any(GetIndexRequest.class), any(RequestOptions.class));
        doReturn(new AcknowledgedResponse(false)).when(indices).putMapping(any(PutMappingRequest.class),
                any(RequestOptions.class));
        doReturn(indices).when(this.client).indices();

        this.index.createIndex(Map.of("map1", "{\"attribute\":\"value\"}"));

        verify(indices, times(1)).exists(any(GetIndexRequest.class), any(RequestOptions.class));
        verify(indices, times(1)).putMapping(any(PutMappingRequest.class), any(RequestOptions.class));
        verify(indices, times(0)).create(any(CreateIndexRequest.class), any(RequestOptions.class));
    }

    @Test
    public void testCreateIndex_Update_ThrowIOException() throws IOException {
        var indices = mock(IndicesClient.class);
        doReturn(true).when(indices).exists(any(GetIndexRequest.class), any(RequestOptions.class));
        doThrow(new IOException()).when(indices).putMapping(any(PutMappingRequest.class), any(RequestOptions.class));
        doReturn(indices).when(this.client).indices();

        this.index.createIndex(Map.of("map1", "{\"attribute\":\"value\"}"));

        verify(indices, times(1)).exists(any(GetIndexRequest.class), any(RequestOptions.class));
        verify(indices, times(1)).putMapping(any(PutMappingRequest.class), any(RequestOptions.class));
        verify(indices, times(0)).create(any(CreateIndexRequest.class), any(RequestOptions.class));
    }

    @Test
    public void testIndex_Insert() throws IOException, IndexingException, JSONException {
        SearchHit[] arr = {};
        var hits = mock(SearchHits.class);
        doReturn(arr).when(hits).getHits();
        var searchResponse = mock(SearchResponse.class);
        doReturn(hits).when(searchResponse).getHits();
        doReturn(searchResponse).when(this.client).search(any(SearchRequest.class), any(RequestOptions.class));

        var indexResponse = mock(IndexResponse.class);
        doReturn("indexId").when(indexResponse).getId();
        doReturn(indexResponse).when(this.client).index(any(IndexRequest.class), any(RequestOptions.class));

        var result = this.index.index("indexName", new JSONObject("{\"id\":\"id\"}"));
        assertThat(result, is("indexId"));

        verify(this.client, times(1)).search(any(SearchRequest.class), any(RequestOptions.class));
        verify(this.client, times(1)).index(any(IndexRequest.class), any(RequestOptions.class));
        verify(this.client, times(0)).update(any(UpdateRequest.class), any(RequestOptions.class));
    }

    @Test
    public void testIndex_Insert_ThrowIOException() throws IOException, IndexingException, JSONException {
        SearchHit[] arr = {};
        var hits = mock(SearchHits.class);
        doReturn(arr).when(hits).getHits();
        var searchResponse = mock(SearchResponse.class);
        doReturn(hits).when(searchResponse).getHits();
        doReturn(searchResponse).when(this.client).search(any(SearchRequest.class), any(RequestOptions.class));

        doThrow(new IOException()).when(this.client).index(any(IndexRequest.class), any(RequestOptions.class));

        var ex = assertThrows(IndexingException.class, () -> this.index.index("indexName", new JSONObject("{\"id\":\"id\"}")));
        assertThat(ex.getMessage(), is("Failed indexing document in index: indexName"));
        assertThat(ex.getCause(), instanceOf(IOException.class));
    }

    @Test
    public void testIndex_Update() throws IOException, IndexingException, JSONException {
        SearchHit[] arr = { mock(SearchHit.class) };
        var hits = mock(SearchHits.class);
        doReturn(arr).when(hits).getHits();
        var searchResponse = mock(SearchResponse.class);
        doReturn(hits).when(searchResponse).getHits();
        doReturn(searchResponse).when(this.client).search(any(SearchRequest.class), any(RequestOptions.class));

        var updateResponse = mock(UpdateResponse.class);
        doReturn("updateId").when(updateResponse).getId();
        doReturn(updateResponse).when(this.client).update(any(UpdateRequest.class), any(RequestOptions.class));

        var result = this.index.index("indexName", new JSONObject("{\"id\":\"id\"}"));
        assertThat(result, is("updateId"));

        verify(this.client, times(1)).search(any(SearchRequest.class), any(RequestOptions.class));
        verify(this.client, times(0)).index(any(IndexRequest.class), any(RequestOptions.class));
        verify(this.client, times(1)).update(any(UpdateRequest.class), any(RequestOptions.class));
    }

    @Test
    public void testIndex_Update_ThrowIOException() throws IOException, IndexingException, JSONException {
        SearchHit[] arr = { mock(SearchHit.class) };
        var hits = mock(SearchHits.class);
        doReturn(arr).when(hits).getHits();
        var searchResponse = mock(SearchResponse.class);
        doReturn(hits).when(searchResponse).getHits();
        doReturn(searchResponse).when(this.client).search(any(SearchRequest.class), any(RequestOptions.class));

        doThrow(new IOException()).when(this.client).update(any(UpdateRequest.class), any(RequestOptions.class));

        var ex = assertThrows(IndexingException.class, () -> this.index.index("indexName", new JSONObject("{\"id\":\"id\"}")));
        assertThat(ex.getMessage(), is("Failed updating document in index: indexName"));
        assertThat(ex.getCause(), instanceOf(IOException.class));
    }

    @Test
    public void testIndex_ThrowIOException() throws IOException, IndexingException, JSONException {
        doThrow(new IOException()).when(this.client).search(any(SearchRequest.class), any(RequestOptions.class));

        var ex = assertThrows(IndexingException.class, () -> this.index.index("indexName", new JSONObject("{\"id\":\"id\"}")));
        assertThat(ex.getMessage(), is("Fail to search index."));
        assertThat(ex.getCause(), instanceOf(IOException.class));
    }
}
