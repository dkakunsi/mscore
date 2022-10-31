package com.devit.mscore.messaging.kafka;

import static com.devit.mscore.ApplicationContext.setContext;

import com.devit.mscore.Logger;
import com.devit.mscore.Subscriber;
import com.devit.mscore.logging.ApplicationLogger;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.json.JSONObject;

public class KafkaSubscriber implements Subscriber {

  private static final Logger LOG = ApplicationLogger.getLogger(KafkaSubscriber.class);

  private Consumer<String, String> consumer;

  private Map<String, java.util.function.Consumer<JSONObject>> topicHandlers;

  private boolean consuming;

  private long pollInterval;

  KafkaSubscriber(Consumer<String, String> consumer, long pollInterval) {
    this.consumer = consumer;
    this.pollInterval = pollInterval;
    this.topicHandlers = new HashMap<>();
  }

  @Override
  public String getChannel() {
    return String.join(",", this.topicHandlers.keySet());
  }

  @Override
  public void subscribe(String topic, java.util.function.Consumer<JSONObject> handler) {
    this.topicHandlers.put(topic, handler);
    LOG.info("Registering kafka consumer for topic {}", topic);
  }

  @Override
  public void start() {
    var topics = this.topicHandlers.keySet();
    this.consumer.subscribe(topics);
    this.consuming = true;

    LOG.info("Start subscribing to {}", topics);

    new Thread(() -> {
      while (consuming) {
        var records = this.consumer.poll(Duration.ofMillis(this.pollInterval));
        records.forEach(this::handleMessage);
      }
    }).start();
  }

  private void handleMessage(ConsumerRecord<String, String> message) {
    new Thread(() -> {
      setContext(KafkaApplicationContext.of(message.headers()));
      var logFormat = "Receiving message from topic '%s', headers: '%s', message: '%s'";
      LOG.info(String.format(logFormat, message.topic(), buildPrintableHeader(message.headers()), message.value()));
      this.topicHandlers.get(message.topic()).accept(new JSONObject(message.value()));
    }).start();
  }

  private List<Pair<String, String>> buildPrintableHeader(Headers headers) {
    var arr = headers.toArray();
    if (arr == null || arr.length <= 0) {
      return List.of();
    }
    return List.of(arr).stream()
        .map(x -> headerToPair(x))
        .collect(Collectors.toList());
  }

  private Pair<String, String> headerToPair(Header header) {
    try {
      return Pair.of(header.key(), new String(header.value(), StandardCharsets.UTF_8.name()));
    } catch (UnsupportedEncodingException ex) {
      throw new RuntimeException(ex);
    }
  }

  @Override
  public void stop() {
    this.consuming = false;
  }
}
