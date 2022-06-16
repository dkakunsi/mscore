package com.devit.mscore;

import com.devit.mscore.exception.ApplicationException;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Root of event listener.
 * 
 * @author dkakunsi
 */
public abstract class Listener implements Starter {

    private static final Logger LOG = LoggerFactory.getLogger(Listener.class);

    protected Subscriber subscriber;

    protected Listener(Subscriber subscriber) {
        this.subscriber = subscriber;
    }

    /**
     * Consume the message.
     * 
     * @param context applicationContext.
     * @param message to synchronize.
     */
    protected abstract void consume(ApplicationContext context, JSONObject message);

    /**
     * Listen for incoming synchronization message.
     */
    public void listen(String... topics) {
        for (var topic : topics) {
            this.subscriber.subscribe(topic, this::consume);
        }

        start();
    }

    @Override
    public void start() {
        try {
            this.subscriber.start();
        } catch (ApplicationException ex) {
            LOG.error("Cannot start consumer.", ex);
        }
    }

    @Override
    public void stop() {
        this.subscriber.stop();
    }
}
