package com.devit.mscore.messaging.kafka;

import static com.devit.mscore.ApplicationContext.setContext;

import com.devit.mscore.Logger;
import com.devit.mscore.Subscriber;
import com.devit.mscore.logging.ApplicationLogger;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.json.JSONObject;

public class KafkaSubscriber implements Subscriber {

  private static final Logger LOG = ApplicationLogger.getLogger(KafkaSubscriber.class);

  private Consumer<String, String> consumer;

  private Map<String, java.util.function.Consumer<JSONObject>> topicHandlers;

  private boolean consuming;

  private long pollDuration;

  KafkaSubscriber(Consumer<String, String> consumer, long pollDuration) {
    this.consumer = consumer;
    this.pollDuration = pollDuration;
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
        var records = this.consumer.poll(Duration.ofMillis(this.pollDuration));
        records.forEach(this::handleMessage);
      }
    }).start();
  }

  private void handleMessage(ConsumerRecord<String, String> message) {
    new Thread(() -> {
      LOG.info(String.format("Receiving message from topic '%s', partition '%s', offset '%s': %s",
          message.topic(), message.partition(), message.offset(), message.value()));
      setContext(KafkaApplicationContext.of(message.headers()));
      this.topicHandlers.get(message.topic()).accept(new JSONObject(message.value()));
    }).start();
  }

  @Override
  public void stop() {
    this.consuming = false;
  }
}
