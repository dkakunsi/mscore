package com.devit.mscore.history;

import com.devit.mscore.ApplicationContext;
import com.devit.mscore.HistoryException;
import com.devit.mscore.History;
import com.devit.mscore.Listener;
import com.devit.mscore.Subscriber;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventListener extends Listener {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventListener.class);

    private History historyManager;

    private EventListener(Subscriber subscriber) {
        super(subscriber);
    }

    public static EventListener of(Subscriber subscriber) {
        return new EventListener(subscriber);
    }

    public static EventListener of(Subscriber subscriber, History historyManager) {
        return new EventListener(subscriber).with(historyManager);
    }

    public EventListener with(History historyManager) {
        this.historyManager = historyManager;
        return this;
    }

    @Override
    public void consume(ApplicationContext context, JSONObject message) {
        LOGGER.debug("BreadcrumbId: {}. Receive event message: {}", context.getBreadcrumbId(), message);
        try {
            this.historyManager.create(context, message);
        } catch (HistoryException ex) {
            LOGGER.error("BreadcrumbId: {}. Failed to create history", context.getBreadcrumbId(), ex);
        }
    }
}
