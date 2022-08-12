package com.devit.mscore.messaging.kafka;

import static com.devit.mscore.ApplicationContext.setContext;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import com.devit.mscore.Logger;
import com.devit.mscore.Subscriber;
import com.devit.mscore.logging.ApplicationLogger;

import org.apache.kafka.clients.consumer.Consumer;
import org.json.JSONObject;

public class KafkaSubscriber implements Subscriber {

    private static final Logger LOG = new ApplicationLogger(KafkaSubscriber.class);

    private static final Duration POLL_DURATION = Duration.ofMillis(10000);

    private Consumer<String, String> consumer;

    private Map<String, java.util.function.Consumer<JSONObject>> topics;

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
    public void subscribe(String topic, java.util.function.Consumer<JSONObject> consumer) {
        LOG.info("Registering kafka consumer for topic {}", topic);
        this.topics.put(topic, consumer);
    }

    @Override
    public void start() {
        // TODO: need to refactor. Each topic should have it's own consumer
        this.consumer.subscribe(this.topics.keySet());
        this.consuming = true;

        new Thread(() -> {
            while (consuming) {
                var records = this.consumer.poll(POLL_DURATION);

                records.forEach(consumerRecord -> {
                    new Thread(() -> {

                        LOG.debug(String.format("Receiving message from topic '%s', partition '%s', offset '%s': %s",
                            consumerRecord.topic(), consumerRecord.partition(), consumerRecord.offset(), consumerRecord.value()));
                        setContext(KafkaApplicationContext.of(consumerRecord.headers()));

                        this.topics.get(consumerRecord.topic()).accept(new JSONObject(consumerRecord.value()));
                    }).run();
                });
            }
        }).start();
    }

    @Override
    public void stop() {
        this.consuming = false;
    }
}
