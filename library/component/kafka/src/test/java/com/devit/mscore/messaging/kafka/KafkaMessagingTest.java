package com.devit.mscore.messaging.kafka;

import static net.mguenther.kafka.junit.EmbeddedKafkaCluster.provisionWith;
import static net.mguenther.kafka.junit.EmbeddedKafkaClusterConfig.defaultClusterConfig;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import com.devit.mscore.ApplicationContext;
import com.devit.mscore.Configuration;
import com.devit.mscore.DefaultApplicationContext;
import com.devit.mscore.exception.ApplicationException;
import com.devit.mscore.exception.ConfigException;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import net.mguenther.kafka.junit.EmbeddedKafkaCluster;
import net.mguenther.kafka.junit.ObserveKeyValues;
import net.mguenther.kafka.junit.SendValues;

public class KafkaMessagingTest {

  private static final String TEST_TOPIC = "testing";

  private EmbeddedKafkaCluster kafka;

  private KafkaMessagingFactory factory;

  private Configuration configuration;

  private ApplicationContext context;

  private KafkaSubscriber subscriber;

  private Consumer<String, String> consumer;

  @SuppressWarnings("unchecked")
  @Before
  public void setup() throws ConfigException {
    this.kafka = provisionWith(defaultClusterConfig());
    this.kafka.start();

    this.configuration = mock(Configuration.class);
    doReturn("test").when(this.configuration).getServiceName();
    doReturn(Optional.of("test")).when(this.configuration).getConfig("services.test.kafka.group.id");
    doReturn(Optional.of("localhost:9092")).when(this.configuration).getConfig("platform.kafka.bootstrap.servers");
    doReturn(Optional.of("1")).when(this.configuration).getConfig("platform.kafka.acks");
    doReturn(Optional.of("org.apache.kafka.common.serialization.StringSerializer")).when(this.configuration)
        .getConfig("platform.kafka.key.serializer");
    doReturn(Optional.of("org.apache.kafka.common.serialization.StringSerializer")).when(this.configuration)
        .getConfig("platform.kafka.value.serializer");
    doReturn(Optional.of("true")).when(this.configuration).getConfig("platform.kafka.enable.auto.commit");
    doReturn(Optional.of("10000")).when(this.configuration).getConfig("platform.kafka.auto.commit.interval.ms");
    doReturn(Optional.of("1000")).when(this.configuration).getConfig("platform.kafka.poll.interval");
    doReturn(Optional.of("org.apache.kafka.common.serialization.StringDeserializer")).when(this.configuration)
        .getConfig("platform.kafka.key.deserializer");
    doReturn(Optional.of("org.apache.kafka.common.serialization.StringDeserializer")).when(this.configuration)
        .getConfig("platform.kafka.value.deserializer");
    doReturn(Optional.of(TEST_TOPIC)).when(this.configuration).getConfig("platform.kafka.topic.testing");

    this.factory = KafkaMessagingFactory.of(this.configuration);
    this.consumer = mock(Consumer.class);
    this.subscriber = new KafkaSubscriber(consumer, 1000);
    this.context = DefaultApplicationContext.of("test");
  }

  @After
  public void tearDown() {
    this.kafka.stop();
    this.kafka = null;
  }

  class Checker {
    boolean check(String valueToCheck) {
      return StringUtils.isNotBlank(valueToCheck);
    }
  }

  @Test
  public void testSubscribe() throws InterruptedException, ApplicationException {
    try (MockedStatic<ApplicationContext> utilities = Mockito.mockStatic(ApplicationContext.class)) {
      utilities.when(() -> ApplicationContext.getContext()).thenReturn(this.context);

      var checker = spy(new Checker());

      var subscriber = this.factory.subscriber();
      subscriber.subscribe(TEST_TOPIC, theMessage -> {
        checker.check(theMessage.getString("id"));
      });
      subscriber.start();

      this.kafka.send(SendValues.to(TEST_TOPIC, "{\"id\":\"id\"}"));
    }
  }

  @Test
  public void testGetConsumerChannel() {
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
    doReturn(new Header[] { breadcrumbIdHeader, principalHeader }).when(headers).toArray();

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

  @Test
  public void testPublish() throws InterruptedException {
    try (MockedStatic<ApplicationContext> utilities = Mockito.mockStatic(ApplicationContext.class)) {
      utilities.when(() -> ApplicationContext.getContext()).thenReturn(this.context);

      var publisher = this.factory.publisher();
      var message = "{\"id\":\"id\"}";
      publisher.publish(TEST_TOPIC, new JSONObject(message));

      var records = this.kafka.observe(ObserveKeyValues.on(TEST_TOPIC, 1));
      assertThat(records.size(), greaterThan(0));
      assertThat(records.get(0).getValue(), is(message));
    }
  }

  @Test
  public void testPublish_EmptyJson() throws InterruptedException {
    try (MockedStatic<ApplicationContext> utilities = Mockito.mockStatic(ApplicationContext.class)) {
      utilities.when(() -> ApplicationContext.getContext())
          .thenReturn(this.context);

      var publisher = this.factory.publisher();
      publisher.publish(TEST_TOPIC, new JSONObject());

      var records = this.kafka.observe(ObserveKeyValues.on(TEST_TOPIC, 0));
      assertThat(records.size(), is(0));
    }
  }
}
