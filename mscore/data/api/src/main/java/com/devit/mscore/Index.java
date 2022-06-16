package com.devit.mscore;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.devit.mscore.exception.IndexingException;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Interface for indexing capabilities.
 * 
 * @author dkakunsi
 */
public abstract class Index {

    protected final String indexName;

    protected Index(String indexName) {
        this.indexName = indexName;
    }

    /**
     * Add a json object to the index, so it is available for searching.
     * 
     * @param context of request.
     * @param json object to index.
     * @return index ID.
     * @throws IndexingException error in adding the json object to index.
     */
    public abstract String index(ApplicationContext context, JSONObject json) throws IndexingException;

    /**
     * Add a list of json object to the index, so it is available for searching.
     * This is a bulk operation.
     * 
     * @param context of request.
     * @param jsons object to index.
     * @return index ID.
     * @throws IndexingException error in adding the json object to index.
     */
    public List<String> index(ApplicationContext context, JSONArray jsons) throws IndexingException {
        List<String> indeces = new ArrayList<>();
        for (var json : jsons) {
            if (json instanceof JSONObject) {
                indeces.add(index(context, (JSONObject) json));
            }
        }
        return indeces;
    }

    /**
     * Search object in an index based on the specified query.
     * 
     * @param context of request.
     * @param criteria criteria to search the indexed object.
     * @return list of json object that meet the specified query.
     * @throws IndexingException error in searching the index.
     */
    public abstract Optional<JSONArray> search(ApplicationContext context, JSONObject criteria) throws IndexingException;

    /**
     * Get object that indexed on {@code indexName} with the given {@code id}.
     * 
     * @param context of request.
     * @param id object id.
     * @return indexed object.
     * @throws IndexingException error on searching.
     */
    public abstract Optional<JSONObject> get(ApplicationContext context, String id) throws IndexingException;

}