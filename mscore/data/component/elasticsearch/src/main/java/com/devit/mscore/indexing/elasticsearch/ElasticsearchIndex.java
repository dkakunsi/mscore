package com.devit.mscore.indexing.elasticsearch;

import java.util.Optional;

import com.devit.mscore.ApplicationContext;
import com.devit.mscore.Index;
import com.devit.mscore.exception.IndexingException;
import com.devit.mscore.exception.RegistryException;

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
    public String index(ApplicationContext context, JSONObject json) throws IndexingException {
        return this.service.index(context, this.indexName, json);
    }

    @Override
    public Optional<JSONArray> search(ApplicationContext context, JSONObject criteria) throws IndexingException {
        return this.service.search(context, this.indexName, criteria);
    }

    @Override
    public Optional<JSONObject> get(ApplicationContext context, String id) throws IndexingException {
        return this.service.get(context, this.indexName, id);
    }

    public Index build(ApplicationContext context) throws JSONException, RegistryException {
        var content = this.mapping.getString("content");
        this.service.buildIndex(context, this.indexName, content);
        return this;
    }
}
