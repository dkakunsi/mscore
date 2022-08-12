package com.devit.mscore;

import java.util.function.Consumer;

import org.json.JSONObject;

/**
 * Object to subscribe message from messaging channel.
 * 
 * @author dkakunsi
 */
public interface Subscriber extends Starter {

    /**
     * Listen to incoming message on hte provided channel.
     * 
     * @param channel  could be topic or queue.
     * @param consumer function to be executed after receiving message.
     */
    void subscribe(String channel, Consumer<JSONObject> consumer);

    /**
     * 
     * @return channel to subscribe
     */
    String getChannel();

}
