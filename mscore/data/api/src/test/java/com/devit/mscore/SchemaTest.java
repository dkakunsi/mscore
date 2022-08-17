package com.devit.mscore;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.devit.mscore.exception.ResourceException;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.junit.Test;

public class SchemaTest {

  @Test
  public void testGetMessage() throws URISyntaxException, ResourceException {
    var resourceFile = getResourceFile("resource/resource.json");
    var schema = new DummySchema(resourceFile);
    var actual = schema.getMessage();

    assertThat(actual.getString("domain"), is("resource"));
    assertThat(actual.getString("name"), is("resource"));
    assertThat(schema.getDomain(), is("resource"));
    assertTrue(StringUtils.isNotBlank(actual.getString("content")));
  }

  @Test
  public void testGetMessage_StringConstructor() throws URISyntaxException, ResourceException {
    var schema = new DummySchema("resource", "{\"key\":\"value\"}");
    var actual = schema.getMessage();

    assertThat(actual.getString("domain"), is("resource"));
    assertThat(actual.getString("name"), is("resource"));
    assertThat(schema.getDomain(), is("resource"));
    assertTrue(StringUtils.isNotBlank(actual.getString("content")));
  }

  public static File getResourceFile(String resourceName) throws URISyntaxException {
    var resource = SchemaTest.class.getClassLoader().getResource(resourceName);
    return new File(resource.toURI());
  }

  private static class DummySchema extends Schema {

    protected DummySchema(File resourceFile) throws ResourceException {
      super(resourceFile);
    }

    protected DummySchema(String name, String content) {
      super(name, content);
    }

    @Override
    public void validate(JSONObject json) {
    }

    @Override
    public Map<String, List<String>> getReferences() {
      return null;
    }

    @Override
    public Set<String> getReferenceNames() {
      return null;
    }

    @Override
    public List<List<String>> getReferenceDomains() {
      return null;
    }

    @Override
    public List<String> getUniqueAttributes() {
      return null;
    }
  }
}
