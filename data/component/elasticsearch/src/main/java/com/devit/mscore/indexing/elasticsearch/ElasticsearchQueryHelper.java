package com.devit.mscore.indexing.elasticsearch;

import com.devit.mscore.Index.Criteria;
import com.devit.mscore.Index.Criteria.Operator;
import com.devit.mscore.Index.SearchCriteria;

import java.util.Arrays;
import java.util.List;

import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

public class ElasticsearchQueryHelper {

  private ElasticsearchQueryHelper() {
  }

  public static QueryBuilder createQuery(SearchCriteria query) {
    if (isAllQuery(query)) {
      return allQuery();
    }
    return attributesQuery(query.getCriteria());
  }

  private static boolean isAllQuery(SearchCriteria query) {
    return query.getCriteria().isEmpty();
  }

  private static QueryBuilder allQuery() {
    var terminated = attributeQuery(Operator.EQUALS, "status", "Terminated");
    return QueryBuilders.boolQuery().mustNot(terminated);
  }

  private static QueryBuilder attributeQuery(Operator operator, String attribute, String value) {
    switch (operator) {
      case CONTAINS:
        return QueryBuilders.queryStringQuery(String.format("*%s*", value)).field(attribute);
      case EQUALS: // the default is equals operation
      default:
        return QueryBuilders.matchQuery(attribute, value);
    }
  }

  private static QueryBuilder attributesQuery(List<Criteria> criteria) {
    var queryBuilder = QueryBuilders.boolQuery();

    criteria.forEach(c -> {
      var operator = c.getOperator();
      var attribute = c.getAttribute();
      var value = c.getValue();

      queryBuilder.must(attributeQuery(operator, value, 1, attribute.split("\\.")));
    });

    return queryBuilder;
  }

  private static QueryBuilder attributeQuery(Operator operator, String value, int currentDepth, String... path) {

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
