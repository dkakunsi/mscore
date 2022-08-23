package com.devit.mscore;

import com.devit.mscore.exception.ApplicationException;

import org.json.JSONObject;

/**
 * Root of event listener.
 *
 * @author dkakunsi
 */
public abstract class Listener implements Starter {

  protected Subscriber subscriber;

  protected Listener(Subscriber subscriber) {
    this.subscriber = subscriber;
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
      this.subscriber.subscribe(topic, this::consume);
    }

    start();
  }

  @Override
  public void start() throws ApplicationException {
    this.subscriber.start();
  }

  @Override
  public void stop() {
    this.subscriber.stop();
  }
}
