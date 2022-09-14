package com.devit.mscore.messaging.kafka;

import static net.mguenther.kafka.junit.EmbeddedKafkaCluster.provisionWith;
import static net.mguenther.kafka.junit.EmbeddedKafkaClusterConfig.defaultClusterConfig;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.devit.mscore.ApplicationContext;
import com.devit.mscore.Configuration;
import com.devit.mscore.DefaultApplicationContext;
import com.devit.mscore.exception.ConfigException;

import java.io.IOException;
import java.util.Optional;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import net.mguenther.kafka.junit.EmbeddedKafkaCluster;
import net.mguenther.kafka.junit.ObserveKeyValues;

public class KafkaPublisherTest {

  private static final String TEST_TOPIC = "testing";

  private EmbeddedKafkaCluster kafka;

  private KafkaMessagingFactory factory;

  private Configuration configuration;

  private ApplicationContext context;

  @Before
  public void setup() throws IOException, ConfigException {
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
    doReturn(Optional.of(TEST_TOPIC)).when(this.configuration).getConfig("platform.kafka.topic.testing");

    this.factory = KafkaMessagingFactory.of(this.configuration);
    this.context = DefaultApplicationContext.of("test");
  }

  @After
  public void tearDown() {
    this.kafka.stop();
    this.kafka = null;
  }

  @Test
  public void testGetChannel() {
    var publisher = this.factory.publisher(TEST_TOPIC);
    assertThat(publisher.getChannel(), is(TEST_TOPIC));
  }

  @Test
  public void testPublish() throws InterruptedException {
    try (MockedStatic<ApplicationContext> utilities = Mockito.mockStatic(ApplicationContext.class)) {
      utilities.when(() -> ApplicationContext.getContext()).thenReturn(this.context);

      var publisher = this.factory.publisher(TEST_TOPIC);
      var message = "{\"id\":\"id\"}";
      publisher.publish(new JSONObject(message));

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

      var testTopic = "testing";
      var publisher = this.factory.publisher(testTopic);
      publisher.publish(new JSONObject());

      var records = this.kafka.observe(ObserveKeyValues.on(TEST_TOPIC, 0));
      assertThat(records.size(), is(0));
    }
  }
}
