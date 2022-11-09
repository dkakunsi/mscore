package com.devit.mscore;

import static com.devit.mscore.util.Constants.ACTION;
import static com.devit.mscore.util.Constants.DATA;
import static com.devit.mscore.util.Constants.DOMAIN;
import static com.devit.mscore.util.Constants.EVENT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import com.devit.mscore.exception.ApplicationRuntimeException;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

public class EventTest {
  
  @Test
  public void testCreate_WithJsonParameter_LowerCase() {
    var json = new JSONObject();
    json.put(EVENT, "create");
    json.put(DOMAIN, "domain");
    json.put(ACTION, "domain.create");
    var jsonData = new JSONObject("{\"id\":\"id\"}");
    json.put(DATA, jsonData);
    var event = Event.of(json);

    var expected = Event.of(Event.Type.CREATE, DOMAIN, "domain.create", jsonData, null);

    assertNotNull(event);
    assertThat(event.getAction(), is(expected.getAction()));
    assertTrue(event.getData().similar(expected.getData()));
    assertTrue(event.toJson().similar(expected.toJson()));
    assertThat(event.getType(), is(expected.getType()));
    assertThat(event.getDomain(), is(expected.getDomain()));
    assertThat(event.getVariables(), is(expected.getVariables()));
    assertTrue(event.isDomainEvent());
  }

  @Test
  public void testCreate_ElementNotAvailable_ShouldThrowException() {
    var json = new JSONObject();
    json.put(EVENT, "create");
    json.put(DOMAIN, "domain");
    var ex = assertThrows(ApplicationRuntimeException.class, () -> Event.of(json));
    assertTrue(ex.getCause() instanceof JSONException);
  }
}
