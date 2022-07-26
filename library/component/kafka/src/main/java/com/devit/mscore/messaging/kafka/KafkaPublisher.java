package com.devit.mscore.messaging.kafka;

import static com.devit.mscore.ApplicationContext.getContext;
import static com.devit.mscore.util.AttributeConstants.getId;
import static com.devit.mscore.util.Constants.ACTION;
import static com.devit.mscore.util.Constants.BREADCRUMB_ID;
import static com.devit.mscore.util.Constants.EVENT_TYPE;
import static com.devit.mscore.util.Constants.PRINCIPAL;

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

  KafkaPublisher(Producer<String, String> producer) {
    this.producer = producer;
  }

  @Override
  public void publish(String channel, JSONObject message) {
    if (StringUtils.isEmpty(channel)) {
      LOG.warn("Cannot publish message. Topic is not provided");
      return;
    }
    if (message == null || message.isEmpty()) {
      LOG.warn("Cannot publish an empty message");
      return;
    }

    // @formatter:off
    var headerPairs = buildHeaderPairs();
    var headers = headerPairs.stream().map(p -> createHeader(p.getKey(), p.getValue())).collect(Collectors.toList());
    LOG.info("Publishing message to topic '{}'. Headers: '{}'. Message: '{}'", channel, headerPairs, message);
    var producerRecord = new ProducerRecord<String, String>(channel, null, getId(message), message.toString(), headers);
    producer.send(producerRecord);
    LOG.info("Message is published");
    // @formatter:on
  }

  private static List<Pair<String, String>> buildHeaderPairs() {
    var context = getContext();
    var headerPairs = new ArrayList<Pair<String, String>>();
    Optional.of(context.getBreadcrumbId()).ifPresent(b -> headerPairs.add(Pair.of(BREADCRUMB_ID, b)));
    context.getPrincipal().ifPresent(p -> headerPairs.add(Pair.of(PRINCIPAL, p.toString())));
    context.getEventType().ifPresent(et -> headerPairs.add(Pair.of(EVENT_TYPE, et)));
    context.getAction().ifPresent(a -> headerPairs.add(Pair.of(ACTION, a)));
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
