package com.devit.mscore.indexing.elasticsearch;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Optional;

import com.devit.mscore.exception.IndexingException;
import com.devit.mscore.exception.RegistryException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

public class ElasticsearchIndexTest {

  private ElasticsearchService service;

  private JSONObject mapping;

  private ElasticsearchIndex index;

  @Before
  public void setup() {
    this.service = mock(ElasticsearchService.class);
    this.mapping = new JSONObject();
    this.mapping.put("content", "{\"id\":\"id\"}");
    this.index = new ElasticsearchIndex("indexName", this.service, this.mapping);
  }

  @Test
  public void testIndex() throws IndexingException {
    doReturn("indexId").when(this.service).index(eq("indexName"), any(JSONObject.class));
    var id = this.index.index(new JSONObject());
    assertThat(id, is("indexId"));
  }

  @Test
  public void testSearch() throws IndexingException {
    doReturn(Optional.of(new JSONArray())).when(this.service).search(eq("indexName"), any(JSONObject.class));
    var result = this.index.search(new JSONObject());
    assertTrue(result.isPresent());
  }

  @Test
  public void testGet() throws IndexingException {
    doReturn(Optional.of(new JSONObject())).when(this.service).get("indexName", "id");
    var result = this.index.get("id");
    assertTrue(result.isPresent());
  }

  @Test
  public void testBuild() throws JSONException, RegistryException {
    this.index.build();
    verify(this.service).buildIndex(anyString(), anyString());
  }
}
