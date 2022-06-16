package com.devit.mscore.notification;

import java.util.ArrayList;
import java.util.List;

import com.devit.mscore.ApplicationContext;
import com.devit.mscore.Listener;
import com.devit.mscore.Notification;
import com.devit.mscore.Subscriber;
import com.devit.mscore.exception.NotificationException;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventListener extends Listener {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventListener.class);

    private List<Notification> notifications;

    private EventListener(Subscriber subscriber) {
        super(subscriber);
        this.notifications = new ArrayList<>();
    }

    public static EventListener of(Subscriber subscriber) {
        return new EventListener(subscriber);
    }

    public static EventListener of(Subscriber subscriber, Notification notification) {
        return new EventListener(subscriber).with(notification);
    }

    public EventListener with(Notification notification) {
        this.notifications.add(notification);
        return this;
    }

    @Override
    public void consume(ApplicationContext context, JSONObject message) {
        LOGGER.debug("BreadcrumbId: {}. Receive event message: {}", context.getBreadcrumbId(), message);
        this.notifications.forEach(notification -> {
            try {
                LOGGER.info("BreadcrumbId: {}. Sending '{}' notification.", context.getBreadcrumbId(), notification.getType());
                notification.send(context, message);
            } catch (NotificationException ex) {
                LOGGER.error("BreadcrumbId: {}. Notification failed.", context.getBreadcrumbId(), ex);
            }
        });
    }
}
