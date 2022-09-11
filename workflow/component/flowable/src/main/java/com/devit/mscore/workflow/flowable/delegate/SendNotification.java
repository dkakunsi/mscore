package com.devit.mscore.workflow.flowable.delegate;

import static com.devit.mscore.ApplicationContext.setContext;
import static com.devit.mscore.util.AttributeConstants.getId;
import static com.devit.mscore.workflow.flowable.delegate.DelegateUtils.NOTIFICATION;

import com.devit.mscore.Logger;
import com.devit.mscore.logging.ApplicationLogger;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.json.JSONObject;

public class SendNotification implements JavaDelegate {

  private static final Logger LOGGER = ApplicationLogger.getLogger(SendNotification.class);

  @Override
  @SuppressWarnings("PMD.GuardLogStatement")
  public void execute(DelegateExecution execution) {
    var context = (FlowableApplicationContext) FlowableApplicationContext.of(execution);
    setContext(context);
    var publisher = context.getPublisher(NOTIFICATION);
    var entity = execution.getVariable("entity", String.class);
    var json = new JSONObject(entity);

    LOGGER.info("Sending notification for entity {}.", getId(json));
    publisher.publish(json);
  }
}
