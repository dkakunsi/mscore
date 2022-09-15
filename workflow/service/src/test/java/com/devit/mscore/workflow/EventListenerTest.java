package com.devit.mscore.workflow;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.devit.mscore.Event;
import com.devit.mscore.Subscriber;
import com.devit.mscore.WorkflowInstance;
import com.devit.mscore.WorkflowService;
import com.devit.mscore.exception.ProcessException;
import com.devit.mscore.exception.RegistryException;

import org.json.JSONObject;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class EventListenerTest {

  @Test
  public void testConsume_DomainEvent() throws ProcessException {
    var subscriber = mock(Subscriber.class);
    var service = mock(WorkflowService.class);
    var instance = mock(WorkflowInstance.class);
    doReturn("instanceId").when(instance).getId();
    doReturn(instance).when(service).createInstanceByAction(anyString(), any(JSONObject.class), anyMap());
    var listener = EventListener.of(subscriber, service);

    var message = new JSONObject();
    message.put(Event.EVENT, "create");
    message.put(Event.DOMAIN, "domain");
    var jsonData = new JSONObject("{\"id\":\"id\"}");
    message.put(Event.DATA, jsonData);

    listener.consume(message);

    var actionCaptor = ArgumentCaptor.forClass(String.class);
    var jsonCaptor = ArgumentCaptor.forClass(JSONObject.class);
    verify(service).createInstanceByAction(actionCaptor.capture(), jsonCaptor.capture(), anyMap());
    assertThat(actionCaptor.getValue(), is("domain.create"));
    assertTrue(jsonCaptor.getValue().similar(jsonData));
  }

  @Test
  public void testConsume_DomainEvent_ActionNotFound_ShouldFail() throws ProcessException {
    var subscriber = mock(Subscriber.class);
    var service = mock(WorkflowService.class);
    doThrow(new ProcessException(new RegistryException(""))).when(service)
      .createInstanceByAction(anyString(), any(JSONObject.class), anyMap());
    var listener = EventListener.of(subscriber, service);

    var message = new JSONObject();
    message.put(Event.EVENT, "create");
    message.put(Event.DOMAIN, "domain");
    var jsonData = new JSONObject("{\"id\":\"id\"}");
    message.put(Event.DATA, jsonData);

    listener.consume(message);

    var actionCaptor = ArgumentCaptor.forClass(String.class);
    var jsonCaptor = ArgumentCaptor.forClass(JSONObject.class);
    verify(service).createInstanceByAction(actionCaptor.capture(), jsonCaptor.capture(), anyMap());
    assertThat(actionCaptor.getValue(), is("domain.create"));
    assertTrue(jsonCaptor.getValue().similar(jsonData));
  }

  @Test
  public void testConsume_DomainEvent_ProcessError_ShouldFail() throws ProcessException {
    var subscriber = mock(Subscriber.class);
    var service = mock(WorkflowService.class);
    doThrow(ProcessException.class).when(service)
      .createInstanceByAction(anyString(), any(JSONObject.class), anyMap());
    var listener = EventListener.of(subscriber, service);

    var message = new JSONObject();
    message.put(Event.EVENT, "create");
    message.put(Event.DOMAIN, "domain");
    var jsonData = new JSONObject("{\"id\":\"id\"}");
    message.put(Event.DATA, jsonData);

    listener.consume(message);

    var actionCaptor = ArgumentCaptor.forClass(String.class);
    var jsonCaptor = ArgumentCaptor.forClass(JSONObject.class);
    verify(service).createInstanceByAction(actionCaptor.capture(), jsonCaptor.capture(), anyMap());
    assertThat(actionCaptor.getValue(), is("domain.create"));
    assertTrue(jsonCaptor.getValue().similar(jsonData));
  }

  @Test
  public void testConsume_TaskEvent() throws ProcessException {
    var subscriber = mock(Subscriber.class);
    var service = mock(WorkflowService.class);
    var listener = EventListener.of(subscriber, service);

    var message = new JSONObject();
    message.put(Event.EVENT, "complete");
    message.put(Event.DOMAIN, "domain");
    var jsonData = new JSONObject();
    jsonData.put("id", "id");
    var response = new JSONObject("{\"approved\":true}");
    jsonData.put("response", response);
    message.put(Event.DATA, jsonData);

    listener.consume(message);

    var idCaptor = ArgumentCaptor.forClass(String.class);
    var jsonCaptor = ArgumentCaptor.forClass(JSONObject.class);
    verify(service).completeTask(idCaptor.capture(), jsonCaptor.capture());
    assertThat(idCaptor.getValue(), is("id"));
    assertTrue(jsonCaptor.getValue().similar(response));
  }
}
