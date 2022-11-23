package com.devit.mscore;

import com.devit.mscore.exception.IndexingException;
import com.devit.mscore.logging.ApplicationLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Interface for indexing capabilities.
 *
 * @author dkakunsi
 */
public abstract class Index {

  protected Logger logger = ApplicationLogger.getLogger(getClass());

  protected final String indexName;

  protected Index(String indexName) {
    this.indexName = indexName;
  }

  public String getName() {
    return indexName;
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
    logger.info("Indexing multiple documents");
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
  @Deprecated
  public Optional<JSONArray> search(JSONObject criteria) throws IndexingException {
    return search(SearchCriteria.from(criteria));
  }

  /**
   * Search object in an index based on the specified query.
   *
   * @param criteria criteria to search the indexed object.
   * @return list of json object that meet the specified query.
   * @throws IndexingException error in searching the index.
   */
  public Optional<JSONArray> search(SearchCriteria criteria) throws IndexingException {
    throw new UnsupportedOperationException();
  }

  /**
   * Get object that indexed on {@code indexName} with the given {@code id}.
   *
   * @param id object id.
   * @return indexed object.
   * @throws IndexingException error on searching.
   */
  public abstract Optional<JSONObject> get(String id) throws IndexingException;

  public static class SearchCriteria {

    protected static final String OPERATOR = "operator";

    protected static final String ATTRIBUTE = "attribute";
  
    protected static final String VALUE = "value";
  
    private static final Logger LOGGER = ApplicationLogger.getLogger(SearchCriteria.class);

    private Operator operator;

    private String attribute;

    private String value;

    public SearchCriteria(Operator operator, String attribute, String value) {
      this.operator = operator;
      this.attribute = attribute;
      this.value = value;
    }

    public Operator getOperator() {
      return operator;
    }

    public String getAttribute() {
      return attribute;
    }

    public String getValue() {
      return value;
    }

    public void validate() throws IndexingException {
      if (operator == null) {
        LOGGER.error("No search operator specified");
        throw new IndexingException("Search operator is not specified");
      }
      if (StringUtils.isBlank(attribute)) {
        LOGGER.error("No search attribute specified");
        throw new IndexingException("Search operator is not specified");
      }
      if (StringUtils.isBlank(value)) {
        LOGGER.error("No search value specified");
        throw new IndexingException("Search value is not specified");
      }
    }

    public boolean isEqualitySearch() {
      return Operator.EQUALS.equals(operator);
    }

    public static SearchCriteria from(JSONObject criteria) {
      var operator = criteria.getString(OPERATOR);
      var attribute = criteria.getString(ATTRIBUTE);
      var value = criteria.getString(VALUE);
      return new SearchCriteria(Operator.valueOf(operator.toUpperCase()), attribute, value);
    }
  
    public static enum Operator {
      EQUALS("equals"), CONTAINS("contains");
  
      private String name;
  
      private Operator(String name) {
        this.name = name;
      }
  
      @Override
      public String toString() {
        return name;
      }
    }
  }
}