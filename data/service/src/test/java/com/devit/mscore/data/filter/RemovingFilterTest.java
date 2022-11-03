package com.devit.mscore.data.filter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.json.JSONObject;
import org.junit.Test;

public class RemovingFilterTest {

  @Test
  public void testFilter() {
    var filter = new RemovingFilter(List.of("password"));
    assertThat(filter.getDomain(), is("all"));

    var json = new JSONObject("{\"domain\":\"domain\",\"attribute\":\"attribute\",\"password\":\"123\"}");
    filter.filter(json);
    assertTrue(json.has("domain"));
    assertTrue(json.has("attribute"));
    assertFalse(json.has("password"));
  }
}
