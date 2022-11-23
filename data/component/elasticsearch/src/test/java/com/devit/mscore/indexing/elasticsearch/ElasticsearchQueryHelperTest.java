package com.devit.mscore.indexing.elasticsearch;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import com.devit.mscore.Index;
import com.devit.mscore.Index.Criteria.Operator;

import java.util.List;

import org.json.JSONObject;
import org.junit.Test;

public class ElasticsearchQueryHelperTest {

  @Test
  public void testCreateQuery_AllQuery() {
    var query = new Index.SearchCriteria(List.of(), 0, 1);
    var queryBuilder = ElasticsearchQueryHelper.createQuery(query);
    var actual = (JSONObject) new JSONObject(queryBuilder.toString()).query("/bool/must_not/0/match/status");
    assertThat(actual.getString("query"), is("Terminated"));
  }

  @Test
  public void testCreateQuery_AttributesQuery_Contains() {
    var query = new Index.SearchCriteria(List.of(new Index.Criteria(Operator.CONTAINS, "attribute", "value")), 0, 1);
    var queryBuilder = ElasticsearchQueryHelper.createQuery(query);
    var actual = (JSONObject) new JSONObject(queryBuilder.toString()).query("/bool/must/0/query_string");
    assertThat(actual.getJSONArray("fields").length(), is(1));
    assertThat(actual.getJSONArray("fields").getString(0), is("attribute^1.0"));
    assertThat(actual.getString("query"), is("*value*"));
  }

  @Test
  public void testCreateQuery_AttributesQuery_Equals() {
    var query = new Index.SearchCriteria(List.of(new Index.Criteria(Operator.EQUALS, "attribute", "value")), 0, 1);
    var queryBuilder = ElasticsearchQueryHelper.createQuery(query);
    var actual = (JSONObject) new JSONObject(queryBuilder.toString()).query("/bool/must/0/match/attribute");
    assertThat(actual.getString("query"), is("value"));
  }

  @Test
  public void testCreateQuery_AttributesQuery_NestedEquals() {
    var query = new Index.SearchCriteria(List.of(new Index.Criteria(Operator.EQUALS, "nested.attribute", "value")), 0, 1);
    var queryBuilder = ElasticsearchQueryHelper.createQuery(query);
    var actualNesting = (JSONObject) new JSONObject(queryBuilder.toString()).query("/bool/must/0/nested");
    assertThat(actualNesting.getString("path"), is("nested"));
    var actualValue = (JSONObject) new JSONObject(queryBuilder.toString())
        .query("/bool/must/0/nested/query/match/nested.attribute");
    assertThat(actualValue.getString("query"), is("value"));
  }

  @Test
  public void testCreateQuery_AttributesQuery_NestedContains() {
    var query = new Index.SearchCriteria(List.of(new Index.Criteria(Operator.CONTAINS, "nested.attribute", "value")), 0, 1);
    var queryBuilder = ElasticsearchQueryHelper.createQuery(query);
    var actualNesting = (JSONObject) new JSONObject(queryBuilder.toString()).query("/bool/must/0/nested");
    assertThat(actualNesting.getString("path"), is("nested"));
    var actualValue = (JSONObject) new JSONObject(queryBuilder.toString())
        .query("/bool/must/0/nested/query/query_string");
    assertThat(actualValue.getJSONArray("fields").length(), is(1));
    assertThat(actualValue.getJSONArray("fields").getString(0), is("nested.attribute^1.0"));
    assertThat(actualValue.getString("query"), is("*value*"));
  }

  @Test
  public void testCreateQuery_MultiCriteria_AttributesQuery_Nested() {
    var criteria = List.of(new Index.Criteria(Operator.EQUALS, "nested.attribute2", "value2"), new Index.Criteria(Operator.CONTAINS, "nested.attribute1", "value1"));
    var query = new Index.SearchCriteria(criteria, 0, 1);
    var queryBuilder = ElasticsearchQueryHelper.createQuery(query);
    var queryBuilderJson = new JSONObject(queryBuilder.toString());

    var actualNesting1 = (JSONObject) queryBuilderJson.query("/bool/must/1/nested");
    assertThat(actualNesting1.getString("path"), is("nested"));

    var actualValue1 = (JSONObject) queryBuilderJson.query("/bool/must/1/nested/query/query_string");
    assertThat(actualValue1.getJSONArray("fields").length(), is(1));
    assertThat(actualValue1.getJSONArray("fields").getString(0), is("nested.attribute1^1.0"));
    assertThat(actualValue1.getString("query"), is("*value1*"));

    var actualNesting2 = (JSONObject) queryBuilderJson.query("/bool/must/0/nested");
    assertThat(actualNesting2.getString("path"), is("nested"));

    var actualValue2 = (JSONObject) queryBuilderJson.query("/bool/must/0/nested/query/match/nested.attribute2");
    assertThat(actualValue2.getString("query"), is("value2"));
  }

  @Test
  public void testBuildPath() {
    var path = ElasticsearchQueryHelper.buildPath(1, "nested.attribute".split("\\."));
    assertThat(path, is("nested"));
    path = ElasticsearchQueryHelper.buildPath(2, "nested.attribute".split("\\."));
    assertThat(path, is("nested.attribute"));
  }
}
