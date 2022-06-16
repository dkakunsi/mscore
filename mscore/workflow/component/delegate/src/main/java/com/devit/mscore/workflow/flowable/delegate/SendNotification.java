package com.devit.mscore.workflow.flowable.delegate;

import static com.devit.mscore.util.AttributeConstants.getId;
import static com.devit.mscore.workflow.flowable.DelegateUtils.NOTIFICATION;

import com.devit.mscore.workflow.flowable.FlowableApplicationContext;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SendNotification implements JavaDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(SendNotification.class);

    @Override
    public void execute(DelegateExecution execution) {
        var context = (FlowableApplicationContext) FlowableApplicationContext.of(execution);
        var publisher = context.getPublisher(NOTIFICATION);
        var entity = execution.getVariable("entity", String.class);
        var json = new JSONObject(entity);

        LOGGER.info("BreadcrumbId: {}. Sending notification for entity {}.", context.getBreadcrumbId(), getId(json));
        publisher.publish(context, json);
    }
}
