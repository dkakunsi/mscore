package com.devit.mscore.indexing.elasticsearch;

import java.util.Arrays;

import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.json.JSONArray;
import org.json.JSONObject;

public class ElasticsearchQueryHelper {

  private static final String EQUALS = "equals";

  private static final String ATTRIBUTE = "attribute";

  private static final String CRITERIA = "criteria";

  private ElasticsearchQueryHelper() {
  }

  public static QueryBuilder createQuery(JSONObject query) {
    if (isAllQuery(query)) {
      return allQuery();
    }
    return attributesQuery(query.getJSONArray(CRITERIA));
  }

  private static boolean isAllQuery(JSONObject query) {
    return query.getJSONArray(CRITERIA).isEmpty();
  }

  private static QueryBuilder allQuery() {
    var terminated = attributeQuery(EQUALS, "status", "Terminated");
    return QueryBuilders.boolQuery().mustNot(terminated);
  }

  private static QueryBuilder attributeQuery(String operator, String attribute, String value) {
    switch (operator) {
      case "contains":
        return QueryBuilders.queryStringQuery(String.format("*%s*", value)).field(attribute);
      case EQUALS: // the default is equals operation
      default:
        return QueryBuilders.matchQuery(attribute, value);
    }
  }

  private static QueryBuilder attributesQuery(JSONArray criteria) {
    var queryBuilder = QueryBuilders.boolQuery();

    criteria.forEach(criterion -> {
      var json = (JSONObject) criterion;
      var operator = json.getString("operator");
      var attribute = json.getString(ATTRIBUTE);
      var value = json.getString("value");

      queryBuilder.must(attributeQuery(operator, value, 1, attribute.split("\\.")));
    });

    return queryBuilder;
  }

  private static QueryBuilder attributeQuery(String operator, String value, int currentDepth, String... path) {

    if (currentDepth < path.length) {
      // Handling nested query.
      return QueryBuilders.nestedQuery(buildPath(currentDepth, path),
          attributeQuery(operator, value, currentDepth + 1, path), ScoreMode.Avg);
    }
    return attributeQuery(operator, buildPath(currentDepth, path), value);
  }

  static String buildPath(int index, String... path) {
    var arr = Arrays.copyOfRange(path, 0, index);
    return String.join(".", arr);
  }
}
