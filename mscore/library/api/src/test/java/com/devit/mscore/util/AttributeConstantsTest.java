package com.devit.mscore.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertTrue;

import org.json.JSONObject;
import org.junit.Test;

public class AttributeConstantsTest {

  @Test
  public void testHasCode() {
    var json = new JSONObject("{\"code\":\"code\"}");
    assertTrue(AttributeConstants.hasCode(json));
  }

  @Test
  public void testGetCode() {
    var json = new JSONObject("{\"code\":\"code\"}");
    var actual = AttributeConstants.getCode(json);
    assertThat(actual, is("code"));
  }

  @Test
  public void testHasName() {
    var json = new JSONObject("{\"name\":\"name\"}");
    assertTrue(AttributeConstants.hasName(json));
  }

  @Test
  public void testGetName() {
    var json = new JSONObject("{\"name\":\"name\"}");
    var actual = AttributeConstants.getName(json);
    assertThat(actual, is("name"));
  }

  @Test
  public void testHasDomain() {
    var json = new JSONObject("{\"domain\":\"domain\"}");
    assertTrue(AttributeConstants.hasDomain(json));
  }

  @Test
  public void testGetDomain() {
    var json = new JSONObject("{\"domain\":\"domain\"}");
    var actual = AttributeConstants.getDomain(json);
    assertThat(actual, is("domain"));
  }

  @Test
  public void testHasId() {
    var json = new JSONObject("{\"id\":\"id\"}");
    assertTrue(AttributeConstants.hasId(json));
  }

  @Test
  public void testGetId() {
    var json = new JSONObject("{\"id\":\"id\"}");
    var actual = AttributeConstants.getId(json);
    assertThat(actual, is("id"));
  }
}
