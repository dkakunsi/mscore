package com.devit.mscore.messaging.kafka;

import static com.devit.mscore.util.Utils.ACTION;
import static com.devit.mscore.util.Utils.BREADCRUMB_ID;
import static com.devit.mscore.util.Utils.PRINCIPAL;
import static com.devit.mscore.util.AttributeConstants.getId;

import java.util.ArrayList;
import java.util.List;

import com.devit.mscore.ApplicationContext;
import com.devit.mscore.Publisher;

import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KafkaPublisher implements Publisher {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaPublisher.class);

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
    public void publish(ApplicationContext context, JSONObject json) {

        if (StringUtils.isEmpty(this.topic)) {
            LOG.warn("BreadcrumbId: {}. Cannot publish message. Topic is not provided.", context.getBreadcrumbId());
            return;
        }
        if (json == null || json.isEmpty()) {
            LOG.warn("BreadcrumbId: {}. Cannot publish an empty message.", context.getBreadcrumbId());
            return;
        }

        // @formatter:off
        var headers = buildHeader(context);
        LOG.debug("BreadcrumbId: {}. Publishing message to topic {}. Headers: {}. Message: {}", context.getBreadcrumbId(), this.topic, headers, json);
        var producerRecord = new ProducerRecord<String, String>(this.topic, null, getId(json), json.toString(), headers);
        this.producer.send(producerRecord);
        // @formatter:on
    }

    private static List<Header> buildHeader(ApplicationContext context) {
        List<Header> headers = new ArrayList<>();
        headers.add(new RecordHeader(BREADCRUMB_ID, context.getBreadcrumbId().getBytes()));
        context.getPrincipal().ifPresent(principal -> headers.add(new RecordHeader(PRINCIPAL, principal.toString().getBytes())));
        context.getAction().ifPresent(action -> headers.add(new RecordHeader(ACTION, action.getBytes())));

        return headers;
    }
}
