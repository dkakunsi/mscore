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

import org.apache.commons.lang3.StringUtils;
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
    var headers = buildHeader();
    LOG.info("Publishing message to topic {}. Headers: {}. Message: {}", this.topic, headers, json);
    var producerRecord = new ProducerRecord<String, String>(this.topic, null, getId(json), json.toString(), headers);
    this.producer.send(producerRecord);
    // @formatter:on
  }

  private static List<Header> buildHeader() {
    var context = getContext();

    List<Header> headers = new ArrayList<>();
    Optional.of(context.getBreadcrumbId()).ifPresent(b -> addHeader(headers, BREADCRUMB_ID, b));
    context.getPrincipal().ifPresent(p -> addHeader(headers, PRINCIPAL, p.toString()));
    context.getAction().ifPresent(a -> addHeader(headers, ACTION, a));
    context.getEventType().ifPresent(et -> addHeader(headers, EVENT_TYPE, et));
    return headers;
  }

  private static void addHeader(List<Header> headers, String key, String value) {
    try {
      headers.add(new RecordHeader(key, value.getBytes(StandardCharsets.UTF_8.name())));
    } catch (UnsupportedEncodingException ex) {
      throw new ApplicationRuntimeException(new ConfigException(ex));
    }
  }
}
