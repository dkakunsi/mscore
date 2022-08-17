package com.devit.mscore;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URISyntaxException;

import com.devit.mscore.exception.ResourceException;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.junit.Test;

public class WorkflowDefinitionTest {

  @Test
  public void testGetMessage() throws URISyntaxException, ResourceException {
    var resourceFile = getResourceFile("resource/resource.json");
    var actual = new DummyDefinition(resourceFile).getMessage("definitionId");

    assertThat(actual.getString("domain"), is("resource"));
    assertThat(actual.getString("name"), is("resource.json"));
    assertThat(actual.getString("definitionId"), is("definitionId"));
    assertTrue(StringUtils.isNotBlank(actual.getString("content")));
  }

  @Test
  public void testGetMessage_3Construct() throws URISyntaxException, ResourceException {
    var actual = new DummyDefinition("name", "{\"key\":\"value\"}").getMessage("definitionId");

    assertThat(actual.getString("domain"), is("resource"));
    assertThat(actual.getString("name"), is("name"));
    assertThat(actual.getString("definitionId"), is("definitionId"));
    assertTrue(StringUtils.isNotBlank(actual.getString("content")));
  }

  public static File getResourceFile(String resourceName) throws URISyntaxException {
    var resource = WorkflowDefinitionTest.class.getClassLoader().getResource(resourceName);
    return new File(resource.toURI());
  }

  private static class DummyDefinition extends WorkflowDefinition {

    protected DummyDefinition(File resourceFile) throws ResourceException {
      super(resourceFile);
    }

    protected DummyDefinition(String name, String content) {
      super(name, content);
    }

    @Override
    public JSONObject getMessage(String definitionId) {
      var message = getMessage();
      message.put("definitionId", definitionId);
      return message;
    }

    @Override
    public String getResourceName() {
      return null;
    }
  }
}
