package com.devit.mscore.data;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.devit.mscore.Event;
import com.devit.mscore.Service;
import com.devit.mscore.Subscriber;
import com.devit.mscore.exception.ApplicationException;

import java.util.Map;

import org.json.JSONObject;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class EventListenerTest {

  @Test
  public void testListeningToCreateEvent() throws ApplicationException {
    var subscriber = mock(Subscriber.class);
    var domainService = mock(Service.class);
    var services = Map.of("domain", domainService);
    var eventListener = EventListener.of(subscriber, services);

    var message = new JSONObject();
    message.put(Event.EVENT, "create");
    message.put(Event.DOMAIN, "domain");
    var jsonData = new JSONObject("{\"id\":\"id\"}");
    message.put(Event.DATA, jsonData);

    eventListener.consume(message);

    var argumentCaptor = ArgumentCaptor.forClass(JSONObject.class);
    verify(domainService).save(argumentCaptor.capture());
    var argument = argumentCaptor.getValue();
    assertTrue(argument.similar(jsonData));
  }

  @Test
  public void testListeningToRemoveEvent() throws ApplicationException {
    var subscriber = mock(Subscriber.class);
    var domainService = mock(Service.class);
    var services = Map.of("domain", domainService);
    var eventListener = EventListener.of(subscriber, services);

    var message = new JSONObject();
    message.put(Event.EVENT, "remove");
    message.put(Event.DOMAIN, "domain");
    var jsonData = new JSONObject("{\"id\":\"id\"}");
    message.put(Event.DATA, jsonData);

    eventListener.consume(message);

    var argumentCaptor = ArgumentCaptor.forClass(String.class);
    verify(domainService).delete(argumentCaptor.capture());
    var argument = argumentCaptor.getValue();
    assertTrue("id".equals(argument));
  }
}
