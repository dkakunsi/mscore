package com.devit.mscore.synchronization;

import static com.devit.mscore.util.AttributeConstants.getDomain;
import static com.devit.mscore.util.AttributeConstants.getId;

import com.devit.mscore.ApplicationContext;
import com.devit.mscore.Subscriber;
import com.devit.mscore.Listener;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SynchronizationListener extends Listener {

    private static final Logger LOG = LoggerFactory.getLogger(SynchronizationListener.class);

    protected SynchronizationsExecutor synchronizer;

    public SynchronizationListener(Subscriber subscriber, SynchronizationsExecutor synchronizer) {
        super(subscriber);
        this.synchronizer = synchronizer;
    }

    @Override
    protected void consume(ApplicationContext context, JSONObject message) {
        LOG.info("BreadcrumbId: {}. External dependency {} of {} domain is updated. Trying to sync references.",
                context.getBreadcrumbId(), getId(message), getDomain(message));
        this.synchronizer.execute(context, message);
        LOG.debug("BreadcrumbId: {}. Dependency is synced: {}", context.getBreadcrumbId(), message);
    }
}
