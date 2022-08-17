package com.devit.mscore.web;

/**
 * Publish a handler into a specific path.
 *
 * @author dkakunsi
 */
public interface Endpoint {

  /**
   * Register endpoint to server.
   */
  void register();
}