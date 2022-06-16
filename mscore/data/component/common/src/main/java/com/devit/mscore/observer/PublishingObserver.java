package com.devit.mscore.observer;

import com.devit.mscore.ApplicationContext;
import com.devit.mscore.Publisher;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PublishingObserver implements PostProcessObserver {

    private static final Logger LOG = LoggerFactory.getLogger(PublishingObserver.class);

    private static final String PUBLISHING_ERROR = "BreadcrumbId: {}. Publishing message failed.";

    protected Publisher publisher;

    protected Long delay;

    public PublishingObserver(Publisher publisher, Long delay) {
        this.publisher = publisher;
        this.delay = delay;
    }

    @Override
    public void notify(ApplicationContext context, JSONObject json) {
        if (this.publisher == null) {
            LOG.warn("BreadcrumbId: {}. Publisher is not provided. By pass publishing.", context.getBreadcrumbId());
            return;
        }

        try {
            if (this.delay != null) {
                // Without delay the synchronization chain will not get the correct data.
                Thread.sleep(this.delay);
            }

            LOG.debug("BreadcrumbId: {}. Publishing message to topic {}: {}.", context.getBreadcrumbId(),
                    this.publisher.getChannel(), json);
            this.publisher.publish(context, json);
        } catch (InterruptedException ex) {
            LOG.error(PUBLISHING_ERROR, context.getBreadcrumbId(), ex);
        }
    }
}
