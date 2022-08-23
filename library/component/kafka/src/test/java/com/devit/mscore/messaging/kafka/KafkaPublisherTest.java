package com.devit.mscore.messaging.kafka;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.devit.mscore.ApplicationContext;
import com.devit.mscore.DefaultApplicationContext;

import java.util.HashMap;

import org.apache.kafka.clients.producer.Producer;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

public class KafkaPublisherTest {

  private KafkaPublisher publisher;

  private Producer<String, String> producer;

  private ApplicationContext context;

  @SuppressWarnings("unchecked")
  @Before
  public void setup() {
    this.producer = mock(Producer.class);
    this.publisher = new KafkaPublisher("topic", this.producer);

    var contextData = new HashMap<String, Object>();
    contextData.put("principal", new JSONObject("{\"requestedBy\":\"requestedBy\",\"role\":[\"user\"]}"));
    this.context = DefaultApplicationContext.of("test", contextData);
  }

  @Test
  public void testGetChannel() {
    assertThat(this.publisher.getChannel(), is("topic"));
  }

  @Test
  public void testPublish() {
    try (MockedStatic<ApplicationContext> utilities = Mockito.mockStatic(ApplicationContext.class)) {
      utilities.when(() -> ApplicationContext.getContext())
          .thenReturn(this.context);

      var message = "{\"id\":\"id\"}";
      this.publisher.publish(new JSONObject(message));
      verify(this.producer, times(1)).send(any());
    }
  }

  @Test
  public void testPublish_EmptyJson() {
    this.publisher.publish(new JSONObject());
    verify(this.producer, times(0)).send(any());
  }

  @Test
  public void testPublish_EmptyTopic() {
    var publisher = new KafkaPublisher("", this.producer);
    var message = "{\"id\":\"id\"}";
    publisher.publish(new JSONObject(message));
    verify(this.producer, times(0)).send(any());
  }
}
