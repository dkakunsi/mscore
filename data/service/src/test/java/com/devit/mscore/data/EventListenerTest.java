package com.devit.mscore.data;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.devit.mscore.Event;
import com.devit.mscore.Service;
import com.devit.mscore.Subscriber;
import com.devit.mscore.exception.ApplicationException;
import com.devit.mscore.exception.ApplicationRuntimeException;

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

  @Test
  public void testListeningToTaskCompleteEvent() throws ApplicationException {
    var subscriber = mock(Subscriber.class);
    var domainService = mock(Service.class);
    var services = Map.of("domain", domainService);
    var eventListener = EventListener.of(subscriber, services);

    var message = new JSONObject();
    message.put(Event.EVENT, "complete");
    message.put(Event.DOMAIN, "domain");
    var jsonData = new JSONObject("{\"id\":\"id\"}");
    message.put(Event.DATA, jsonData);

    eventListener.consume(message);

    verify(domainService, never()).delete(any());
    verify(domainService, never()).save(any());
  }

  @Test
  public void testListening_OnError_ShouldLogError() throws ApplicationException {
    var subscriber = mock(Subscriber.class);
    var domainService = mock(Service.class);
    doThrow(ApplicationException.class).when(domainService).delete(anyString());
    var services = Map.of("domain", domainService);
    var eventListener = EventListener.of(subscriber, services);

    var message = new JSONObject();
    message.put(Event.EVENT, "remove");
    message.put(Event.DOMAIN, "domain");
    var jsonData = new JSONObject("{\"id\":\"id\"}");
    message.put(Event.DATA, jsonData);

    eventListener.consume(message);
    verify(domainService).delete(anyString());
  }

  @Test
  public void testCreateListener_NoService_ShouldFailed() throws ApplicationException {
    var subscriber = mock(Subscriber.class);
    Map<String, Service> services = Map.of();

    var ex = assertThrows(ApplicationRuntimeException.class, () -> EventListener.of(subscriber, services));
    assertThat(ex.getMessage(), is("Empty services is passed to EventListener"));
  }
}
