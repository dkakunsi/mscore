package com.devit.mscore;

import com.devit.mscore.exception.ApplicationException;

import java.util.List;

import org.json.JSONObject;

/**
 * Root of event listener.
 *
 * @author dkakunsi
 */
public abstract class Listener implements Starter {

  protected Logger logger;

  protected Subscriber subscriber;

  protected Listener(Subscriber subscriber, Logger logger) {
    this.subscriber = subscriber;
    this.logger = logger;
  }

  /**
   * Consume the message.
   *
   * @param message to synchronize.
   */
  protected abstract void consume(JSONObject message);

  /**
   * Listen for incoming synchronization message.
   *
   * @throws ApplicationException
   */
  public void listen(String... topics) throws ApplicationException {
    for (var topic : topics) {
      subscriber.subscribe(topic, this::consume);
    }
    logger.info("Listening to topics '{}'", List.of(topics));
    start();
  }

  @Override
  public void start() throws ApplicationException {
    subscriber.start();
  }

  @Override
  public void stop() {
    subscriber.stop();
  }
}
