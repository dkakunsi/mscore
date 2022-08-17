package com.devit.mscore;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.junit.Test;

public class DefaultApplicationContextTest {

  @Test
  public void testDataAvailable() {
    var contextData = new HashMap<String, Object>();
    contextData.put("breadcrumbId", "breadcrumbId");
    contextData.put("principal", new JSONObject("{\"requestedBy\":\"requestedBy\"}"));

    var context = DefaultApplicationContext.of("source", contextData);
    assertThat(context.getBreadcrumbId(), is("breadcrumbId"));
    assertThat(context.getRequestedBy(), is("requestedBy"));
    assertThat(context.getSource(), is("source"));
    assertTrue(context.getPrincipal().isPresent());
    assertNotNull(context.toJson());
    assertNotNull(context.toString());
  }

  @Test
  public void testDataAvailable_NoContextData() {
    var context = DefaultApplicationContext.of("source");
    assertFalse(StringUtils.isEmpty(context.getBreadcrumbId()));
    assertThat(context.getRequestedBy(), is("UNKNOWN"));
    assertThat(context.getSource(), is("source"));
    assertNotNull(context.toJson());
    assertNotNull(context.toString());
  }
}
