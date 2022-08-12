package com.devit.mscore;

import com.devit.mscore.exception.NotificationException;

import org.json.JSONObject;

/**
 * Object to send notification to customer.
 * 
 * @author dkakunsi
 */
public interface Notification {

    /**
     * Type of notification. Ex. email, phone, etc.
     * 
     * @return notificationtype.
     */
    String getType();

    /**
     * Build and send notification about {@code entity} changes.
     * 
     * @param entity  to notify about.
     */
    void send(JSONObject entity) throws NotificationException;
}
