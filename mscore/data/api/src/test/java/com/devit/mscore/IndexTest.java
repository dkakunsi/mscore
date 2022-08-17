package com.devit.mscore;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Optional;

import com.devit.mscore.exception.IndexingException;
import com.devit.mscore.util.AttributeConstants;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

public class IndexTest {

  @Test
  public void test() throws IndexingException {
    var jsons = new JSONArray();
    jsons.put(new JSONObject("{\"id\":\"id1\"}"));
    jsons.put(new JSONObject("{\"id\":\"id2\"}"));
    jsons.put(new JSONObject("{\"id\":\"id3\"}"));

    var index = new Index("indexName") {
      @Override
      public String index(JSONObject json) throws IndexingException {
        return AttributeConstants.getId(json);
      }

      @Override
      public Optional<JSONArray> search(JSONObject criteria) throws IndexingException {
        return null;
      }

      @Override
      public Optional<JSONObject> get(String id) throws IndexingException {
        return null;
      }
    };

    var indices = index.index(jsons);
    assertThat(indices.size(), is(3));
    assertThat(indices.get(0), is("id1"));
    assertThat(indices.get(1), is("id2"));
    assertThat(indices.get(2), is("id3"));
  }
}
