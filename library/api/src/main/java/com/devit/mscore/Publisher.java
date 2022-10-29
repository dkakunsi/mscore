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
   * @param channel to publish to
   * @param message to be published.
   */
  void publish(String channel, JSONObject message);
}