package com.devit.mscore.indexing.elasticsearch;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.json.JSONObject;
import org.junit.Test;

public class ElasticsearchQueryHelperTest {

  @Test
  public void testCreateQuery_AllQuery() {
    var query = "{\"domain\":\"domain\",\"criteria\":[]}";
    var queryBuilder = ElasticsearchQueryHelper.createQuery(new JSONObject(query));
    var actual = (JSONObject) new JSONObject(queryBuilder.toString()).query("/bool/must_not/0/match/status");
    assertThat(actual.getString("query"), is("Terminated"));
  }

  @Test
  public void testCreateQuery_AttributesQuery_Contains() {
    var query = "{\"domain\":\"domain\",\"criteria\":[{\"attribute\":\"attribute\",\"value\":\"value\",\"operator\":\"contains\"}]}";
    var queryBuilder = ElasticsearchQueryHelper.createQuery(new JSONObject(query));
    var actual = (JSONObject) new JSONObject(queryBuilder.toString()).query("/bool/must/0/query_string");
    assertThat(actual.getJSONArray("fields").length(), is(1));
    assertThat(actual.getJSONArray("fields").getString(0), is("attribute^1.0"));
    assertThat(actual.getString("query"), is("*value*"));
  }

  @Test
  public void testCreateQuery_AttributesQuery_Equals() {
    var query = "{\"domain\":\"domain\",\"criteria\":[{\"attribute\":\"attribute\",\"value\":\"value\",\"operator\":\"equals\"}]}";
    var queryBuilder = ElasticsearchQueryHelper.createQuery(new JSONObject(query));
    var actual = (JSONObject) new JSONObject(queryBuilder.toString()).query("/bool/must/0/match/attribute");
    assertThat(actual.getString("query"), is("value"));
  }

  @Test
  public void testCreateQuery_AttributesQuery_NestedEquals() {
    var query = "{\"domain\":\"domain\",\"criteria\":[{\"attribute\":\"nested.attribute\",\"value\":\"value\",\"operator\":\"equals\"}]}";
    var queryBuilder = ElasticsearchQueryHelper.createQuery(new JSONObject(query));
    var actualNesting = (JSONObject) new JSONObject(queryBuilder.toString()).query("/bool/must/0/nested");
    assertThat(actualNesting.getString("path"), is("nested"));
    var actualValue = (JSONObject) new JSONObject(queryBuilder.toString())
        .query("/bool/must/0/nested/query/match/nested.attribute");
    assertThat(actualValue.getString("query"), is("value"));
  }

  @Test
  public void testCreateQuery_AttributesQuery_NestedContains() {
    var query = "{\"domain\":\"domain\",\"criteria\":[{\"attribute\":\"nested.attribute\",\"value\":\"value\",\"operator\":\"contains\"}]}";
    var queryBuilder = ElasticsearchQueryHelper.createQuery(new JSONObject(query));
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
    var query = "{\"domain\":\"domain\",\"criteria\":[{\"attribute\":\"nested.attribute1\",\"value\":\"value1\",\"operator\":\"contains\"},{\"attribute\":\"nested.attribute2\",\"value\":\"value2\",\"operator\":\"equals\"}]}";
    var queryBuilder = ElasticsearchQueryHelper.createQuery(new JSONObject(query));
    var actualNesting1 = (JSONObject) new JSONObject(queryBuilder.toString()).query("/bool/must/0/nested");
    assertThat(actualNesting1.getString("path"), is("nested"));
    var actualValue1 = (JSONObject) new JSONObject(queryBuilder.toString())
        .query("/bool/must/0/nested/query/query_string");
    assertThat(actualValue1.getJSONArray("fields").length(), is(1));
    assertThat(actualValue1.getJSONArray("fields").getString(0), is("nested.attribute1^1.0"));
    assertThat(actualValue1.getString("query"), is("*value1*"));
    var actualNesting2 = (JSONObject) new JSONObject(queryBuilder.toString()).query("/bool/must/1/nested");
    assertThat(actualNesting2.getString("path"), is("nested"));
    var actualValue2 = (JSONObject) new JSONObject(queryBuilder.toString())
        .query("/bool/must/1/nested/query/match/nested.attribute2");
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
