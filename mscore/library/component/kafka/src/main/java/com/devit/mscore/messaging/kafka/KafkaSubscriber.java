package com.devit.mscore.messaging.kafka;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import com.devit.mscore.ApplicationContext;
import com.devit.mscore.Subscriber;

import org.apache.kafka.clients.consumer.Consumer;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KafkaSubscriber implements Subscriber {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaSubscriber.class);

    private static final Duration POLL_DURATION = Duration.ofMillis(10000);

    private Consumer<String, String> consumer;

    private Map<String, java.util.function.BiConsumer<ApplicationContext, JSONObject>> topics;

    private boolean consuming;

    KafkaSubscriber(Consumer<String, String> consumer) {
        this.consumer = consumer;
        this.topics = new HashMap<>();
    }

    @Override
    public String getChannel() {
        return String.join(",", this.topics.keySet());
    }

    @Override
    public void subscribe(String topic, java.util.function.BiConsumer<ApplicationContext, JSONObject> consumer) {
        LOG.info("Registering kafka consumer for topic {}", topic);
        this.topics.put(topic, consumer);
    }

    @Override
    public void start() {
        this.consumer.subscribe(this.topics.keySet());
        this.consuming = true;

        new Thread(() -> {
            while (consuming) {
                var records = this.consumer.poll(POLL_DURATION);

                records.forEach(consumerRecord -> {
                    LOG.debug(String.format("Receiving message from topic '%s', partition '%s', offset '%s': %s",
                            consumerRecord.topic(), consumerRecord.partition(), consumerRecord.offset(), consumerRecord.value()));

                    this.topics.get(consumerRecord.topic()).accept(KafkaApplicationContext.of(consumerRecord.headers()), new JSONObject(consumerRecord.value()));
                });
            }
        }).start();
    }

    @Override
    public void stop() {
        this.consuming = false;
    }
}
