package com.devit.mscore.messaging.kafka;

import com.devit.mscore.Configuration;
import com.devit.mscore.exception.ApplicationRuntimeException;
import com.devit.mscore.exception.ConfigException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;

/**
 *
 * @author dkakunsi
 */
public class KafkaMessagingFactory {

  private static final String CONFIG_TEMPLATE = "platform.kafka.%s";

  private static final String TOPIC_TEMPLATE = "platform.kafka.topic.%s";

  private static final String SERVICE_CONFIG_TEMPLATE = "services.%s.kafka.%s";

  private static final List<String> PRODUCER_CONFIG_OPTIONS = Arrays.asList(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
      ProducerConfig.ACKS_CONFIG, ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
      ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG);

  private static final List<String> CONSUMER_CONFIG_OPTIONS = Arrays.asList(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
      ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG,
      ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG);

  private Map<String, KafkaPublisher> kafkaPublishers;

  private KafkaSubscriber kafkaSubscriber;

  private Producer<String, String> producer;

  private Consumer<String, String> consumer;

  private Configuration configuration;

  private Pair<String, String> kafkaClientId;

  private Pair<String, String> kafkaGroupId;

  protected KafkaMessagingFactory(Configuration configuration) throws ConfigException {
    this.kafkaPublishers = new HashMap<>();
    this.configuration = configuration;

    initKafkaIds();
  }

  private void initKafkaIds() throws ConfigException {
    var groupId = getGroupId();
    this.kafkaGroupId = Pair.of(ConsumerConfig.GROUP_ID_CONFIG, groupId);

    var clientId = getClientId(groupId);
    this.kafkaClientId = Pair.of(ProducerConfig.CLIENT_ID_CONFIG, clientId);
  }

  private String getGroupId() throws ConfigException {
    var groupIdConfigKey = String.format(SERVICE_CONFIG_TEMPLATE, this.configuration.getServiceName(),
        ConsumerConfig.GROUP_ID_CONFIG);
    return getConfig(groupIdConfigKey).orElseThrow(() -> new ConfigException("No kafka group id is provided"));
  }

  private String getClientId(String groupId) {
    return String.format("%s-%s", groupId, UUID.randomUUID());
  }

  public static KafkaMessagingFactory of(Configuration configuration) throws ConfigException {
    return new KafkaMessagingFactory(configuration);
  }

  public Producer<String, String> producer() {
    if (this.producer == null) {
      var properties = getProperties(PRODUCER_CONFIG_OPTIONS);
      this.producer = new KafkaProducer<>(properties);
    }
    return this.producer;
  }

  // TODO remove this after implementing embedded-kafka for testing.
  void setProducer(Producer<String, String> producer) {
    this.producer = producer;
  }

  public Consumer<String, String> consumer() {
    if (this.consumer == null) {
      var properties = getProperties(CONSUMER_CONFIG_OPTIONS);
      properties.setProperty(this.kafkaGroupId.getKey(), this.kafkaGroupId.getValue());
      this.consumer = new KafkaConsumer<>(properties);
    }
    return this.consumer;
  }

  // TODO remove this after implementing embedded-kafka for testing.
  void setConsumer(Consumer<String, String> consumer) {
    this.consumer = consumer;
  }

  protected Properties getProperties(List<String> configOptions) {
    var properties = new Properties();
    properties.setProperty(this.kafkaClientId.getKey(), this.kafkaClientId.getValue());
    configOptions.forEach(option -> properties.put(option, getTemplatedConfig(option).orElse(null)));
    return properties;
  }

  private Optional<String> getTemplatedConfig(String key) {
    try {
      return getConfig(String.format(CONFIG_TEMPLATE, key));
    } catch (ConfigException ex) {
      throw new ApplicationRuntimeException(ex);
    }
  }

  protected Optional<String> getConfig(String key) throws ConfigException {
    return this.configuration.getConfig(key);
  }

  /**
   * This will search for `platform.kafka.topic.{@code name}`.
   *
   * @param name topic name.
   * @return kafka topics.
   * @throws ConfigException
   */
  public Optional<String[]> getTemplatedTopics(String name) throws ConfigException {
    var topics = getConfig(String.format(TOPIC_TEMPLATE, name));
    return checkTopicsResult(topics);
  }

  public Optional<String[]> getTopics(String configKey) throws ConfigException {
    var topics = getConfig(configKey);
    return checkTopicsResult(topics);
  }

  private Optional<String[]> checkTopicsResult(Optional<String> resultOptional) {
    if (resultOptional.isEmpty()) {
      return Optional.empty();
    }
    var result = resultOptional.get();
    if (!result.contains(",")) {
      return Optional.of(new String[] { result });
    }
    return Optional.of(result.split(","));
  }

  public KafkaSubscriber subscriber() {
    if (this.kafkaSubscriber == null) {
      this.kafkaSubscriber = new KafkaSubscriber(consumer());
    }
    return this.kafkaSubscriber;
  }

  public KafkaPublisher publisher(String name) {
    this.kafkaPublishers.computeIfAbsent(name, publisherName -> {
      try {
        var topic = getTemplatedTopics(publisherName)
            .orElseThrow(() -> new ApplicationRuntimeException(String.format("No topic for %s", name)));
        return new KafkaPublisher(topic[0], producer());
      } catch (ConfigException ex) {
        throw new ApplicationRuntimeException(ex);
      }
    });
    return this.kafkaPublishers.get(name);
  }
}