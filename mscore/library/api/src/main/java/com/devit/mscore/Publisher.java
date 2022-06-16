package com.devit.mscore;

import org.json.JSONObject;

/**
 * Object to publish message to messaging channel.
 * 
 * @author dkakunsi
 */
public interface Publisher {

    /**
     * Publish message to broker on the provided channel.
     * 
     * @param context application context.
     * @param json to publish.
     */
    void publish(ApplicationContext context, JSONObject json);

    /**
     * 
     * @return channel to publish to.
     */
    String getChannel();
}