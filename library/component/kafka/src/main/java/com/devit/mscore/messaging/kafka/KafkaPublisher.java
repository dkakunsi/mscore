package com.devit.mscore.messaging.kafka;

import static com.devit.mscore.ApplicationContext.getContext;
import static com.devit.mscore.util.AttributeConstants.getId;
import static com.devit.mscore.util.Utils.ACTION;
import static com.devit.mscore.util.Utils.BREADCRUMB_ID;
import static com.devit.mscore.util.Utils.EVENT_TYPE;
import static com.devit.mscore.util.Utils.PRINCIPAL;

import com.devit.mscore.Logger;
import com.devit.mscore.Publisher;
import com.devit.mscore.exception.ApplicationRuntimeException;
import com.devit.mscore.exception.ConfigException;
import com.devit.mscore.logging.ApplicationLogger;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.json.JSONObject;

public class KafkaPublisher implements Publisher {

  private static final Logger LOG = ApplicationLogger.getLogger(KafkaPublisher.class);

  protected Producer<String, String> producer;

  protected String topic;

  KafkaPublisher(String topic, Producer<String, String> producer) {
    this.producer = producer;
    this.topic = topic;
  }

  @Override
  public String getChannel() {
    return this.topic;
  }

  @Override
  public void publish(JSONObject json) {

    if (StringUtils.isEmpty(this.topic)) {
      LOG.warn("Cannot publish message. Topic is not provided");
      return;
    }
    if (json == null || json.isEmpty()) {
      LOG.warn("Cannot publish an empty message");
      return;
    }

    // @formatter:off
    var headerPairs = buildHeaderPairs();
    var headers = headerPairs.stream().map(p -> createHeader(p.getKey(), p.getValue())).collect(Collectors.toList());
    LOG.info("Publishing message to topic {}. Headers: {}. Message: {}", this.topic, headerPairs, json);
    var producerRecord = new ProducerRecord<String, String>(this.topic, null, getId(json), json.toString(), headers);
    this.producer.send(producerRecord);
    // @formatter:on
  }

  private static List<Pair<String, String>> buildHeaderPairs() {
    var context = getContext();
    var headerPairs = new ArrayList<Pair<String, String>>();
    Optional.of(context.getBreadcrumbId()).ifPresent(b -> headerPairs.add(Pair.of(BREADCRUMB_ID, b)));
    context.getPrincipal().ifPresent(p -> headerPairs.add(Pair.of(PRINCIPAL, p.toString())));
    context.getAction().ifPresent(a -> headerPairs.add(Pair.of(ACTION, a)));
    context.getEventType().ifPresent(et -> headerPairs.add(Pair.of(EVENT_TYPE, et)));
    return headerPairs;
  }

  private static Header createHeader(String key, String value) {
    try {
      return new RecordHeader(key, value.getBytes(StandardCharsets.UTF_8.name()));
    } catch (UnsupportedEncodingException ex) {
      throw new ApplicationRuntimeException(new ConfigException(ex));
    }
  }
}
