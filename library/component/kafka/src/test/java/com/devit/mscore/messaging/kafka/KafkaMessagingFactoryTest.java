package com.devit.mscore.messaging.kafka;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.devit.mscore.Configuration;
import com.devit.mscore.exception.ConfigException;

import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

public class KafkaMessagingFactoryTest {

  private KafkaMessagingFactory factory;

  private Configuration configuration;

  @Before
  public void setup() throws ConfigException {
    this.configuration = mock(Configuration.class);
    doReturn(Optional.of("value")).when(this.configuration).getConfig(anyString());

    this.factory = KafkaMessagingFactory.of(this.configuration);
  }

  @Test
  public void testGetProperties() throws ConfigException {
    doReturn(Optional.of("value1")).when(this.configuration).getConfig("platform.kafka.config1");
    var properties = this.factory.getProperties(List.of("config1"));
    assertNotNull(properties);
    assertThat(properties.getProperty("config1"), is("value1"));
  }

  @Test
  public void testGetTemplatedTopics_SingleTopic() throws ConfigException {
    doReturn(Optional.of("topic")).when(this.configuration).getConfig("platform.kafka.topic.name");
    var topics = this.factory.getTemplatedTopics("name");
    assertTrue(topics.isPresent());
    assertThat(topics.get().length, is(1));
    assertThat(topics.get()[0], is("topic"));
  }

  @Test
  public void testGetTemplatedTopics_MultipleTopic() throws ConfigException {
    doReturn(Optional.of("topic1,topic2")).when(this.configuration).getConfig("platform.kafka.topic.name");
    var topics = this.factory.getTemplatedTopics("name");
    assertTrue(topics.isPresent());
    assertThat(topics.get().length, is(2));
    assertThat(topics.get()[0], is("topic1"));
    assertThat(topics.get()[1], is("topic2"));
  }

  @Test
  public void testGetTemplatedTopics_Empty() throws ConfigException {
    doReturn(Optional.empty()).when(this.configuration).getConfig("platform.kafka.topic.name");
    var topics = this.factory.getTemplatedTopics("name");
    assertTrue(topics.isEmpty());
  }

  @Test
  public void testGetTopics() throws ConfigException {
    doReturn(Optional.of("topic1,topic2")).when(this.configuration).getConfig("services.data.kafka.topic.dependency");
    var key = "services.data.kafka.topic.dependency";
    var topics = this.factory.getTopics(key);
    assertThat(topics.get().length, is(2));
  }
}
