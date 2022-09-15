package com.devit.mscore;

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
    json.put(Event.EVENT, "create");
    json.put(Event.DOMAIN, "domain");
    var jsonData = new JSONObject("{\"id\":\"id\"}");
    json.put(Event.DATA, jsonData);
    var event = Event.of(json);
    assertNotNull(event);
    assertThat(event.getAction(), is("domain.create"));
    assertTrue(event.getData().similar(jsonData));
    assertTrue(event.toJson().similar(json));
    assertThat(event.getType(), is(Event.Type.CREATE));
    assertThat(event.getDomain(), is("domain"));
    assertTrue(event.isDomainEvent());
  }

  @Test
  public void testCreate_ElementNotAvailable_ShouldThrowException() {
    var json = new JSONObject();
    json.put(Event.EVENT, "create");
    json.put(Event.DOMAIN, "domain");
    var ex = assertThrows(ApplicationRuntimeException.class, () -> Event.of(json));
    assertTrue(ex.getCause() instanceof JSONException);
  }
}
