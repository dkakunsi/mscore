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
    return this.service.index(this.indexName, json);
  }

  @Override
  public Optional<JSONArray> search(JSONObject criteria) throws IndexingException {
    return this.service.search(this.indexName, criteria);
  }

  @Override
  public Optional<JSONObject> get(String id) throws IndexingException {
    return this.service.get(this.indexName, id);
  }

  public Index build() throws JSONException {
    var content = this.mapping.getString("content");
    this.service.buildIndex(this.indexName, content);
    return this;
  }
}
