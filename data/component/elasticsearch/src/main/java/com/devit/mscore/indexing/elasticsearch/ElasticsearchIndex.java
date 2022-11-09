package com.devit.mscore.indexing.elasticsearch;

import com.devit.mscore.Index;
import com.devit.mscore.exception.IndexingException;

import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ElasticsearchIndex extends Index {

  private JSONObject mapping;

  private ElasticsearchService service;

  ElasticsearchIndex(String indexName, ElasticsearchService service, JSONObject mapping) {
    super(indexName);
    this.service = service;
    this.mapping = mapping;
  }

  @Override
  public String index(JSONObject json) throws IndexingException {
    return service.index(indexName, json);
  }

  @Override
  public Optional<JSONArray> search(JSONObject criteria) throws IndexingException {
    return service.search(indexName, criteria);
  }

  @Override
  public Optional<JSONObject> get(String id) throws IndexingException {
    return service.get(indexName, id);
  }

  public Index build() throws JSONException {
    var content = mapping.getString("content");
    service.buildIndex(indexName, content);
    return this;
  }
}
