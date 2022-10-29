package com.devit.mscore;

import com.devit.mscore.exception.IndexingException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
   * @param json object to index.
   * @return index ID.
   * @throws IndexingException error in adding the json object to index.
   */
  public abstract String index(JSONObject json) throws IndexingException;

  /**
   * Add a list of json object to the index, so it is available for searching.
   * This is a bulk operation.
   *
   * @param jsons object to index.
   * @return index ID.
   * @throws IndexingException error in adding the json object to index.
   */
  public List<String> index(JSONArray jsons) throws IndexingException {
    List<String> indeces = new ArrayList<>();
    for (var json : jsons) {
      if (json instanceof JSONObject) {
        indeces.add(index((JSONObject) json));
      }
    }
    return indeces;
  }

  /**
   * Search object in an index based on the specified query.
   *
   * @param criteria criteria to search the indexed object.
   * @return list of json object that meet the specified query.
   * @throws IndexingException error in searching the index.
   */
  public abstract Optional<JSONArray> search(JSONObject criteria) throws IndexingException;

  /**
   * Get object that indexed on {@code indexName} with the given {@code id}.
   *
   * @param id object id.
   * @return indexed object.
   * @throws IndexingException error on searching.
   */
  public abstract Optional<JSONObject> get(String id) throws IndexingException;

}