package com.devit.mscore.messaging.kafka;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

public class KafkaSubscriberTest {

  private KafkaSubscriber subscriber;

  private Consumer<String, String> consumer;

  @SuppressWarnings("unchecked")
  @Before
  public void setup() {
    this.consumer = mock(Consumer.class);
    this.subscriber = new KafkaSubscriber(consumer);
  }

  @Test
  public void testSubscribe() {
    java.util.function.Consumer<JSONObject> consumer = json -> {
    };
    this.subscriber.subscribe("topic", consumer);
    var topics = this.subscriber.getChannel();
    assertThat(topics, is("topic"));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testConsume() throws InterruptedException {
    var result = new JSONObject();

    java.util.function.Consumer<JSONObject> localConsumer = json -> result.put("json", json);
    this.subscriber.subscribe("topic", localConsumer);
    var topics = this.subscriber.getChannel();
    assertThat(topics, is("topic"));

    var principal = new JSONObject("{\"requestedBy\":\"requestedBy\"}");
    var principalHeader = mock(Header.class);
    doReturn(principal.toString().getBytes()).when(principalHeader).value();

    var breadcrumbId = "breadcrumbId";
    var breadcrumbIdHeader = mock(Header.class);
    doReturn(breadcrumbId.getBytes()).when(breadcrumbIdHeader).value();

    var headers = mock(Headers.class);
    doReturn(principalHeader).when(headers).lastHeader("principal");
    doReturn(breadcrumbIdHeader).when(headers).lastHeader("breadcrumbId");

    var record = mock(ConsumerRecord.class);
    doReturn("topic").when(record).topic();
    doReturn(0).when(record).partition();
    doReturn(0L).when(record).offset();
    doReturn("{\"id\":\"id\"}").when(record).value();
    doReturn(headers).when(record).headers();

    var records = new ConsumerRecords<String, String>(Map.of(new TopicPartition("topic", 0), List.of(record)));

    doReturn(records).when(this.consumer).poll(any(Duration.class));

    this.subscriber.start();
    await().until(new Callable<Boolean>() {
      public Boolean call() throws Exception {
        return result.has("json");
      };
    });
    this.subscriber.stop();

    assertNotNull(result.get("json"));
    assertThat(result.getJSONObject("json").getString("id"), is("id"));
  }
}
