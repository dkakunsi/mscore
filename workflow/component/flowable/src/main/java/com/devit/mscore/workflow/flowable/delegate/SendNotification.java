package com.devit.mscore.workflow.flowable.delegate;

import static com.devit.mscore.ApplicationContext.setContext;
import static com.devit.mscore.util.AttributeConstants.getCode;
import static com.devit.mscore.util.AttributeConstants.getDomain;
import static com.devit.mscore.workflow.flowable.delegate.DelegateUtils.NOTIFICATION;

import com.devit.mscore.Logger;
import com.devit.mscore.logging.ApplicationLogger;

import org.apache.commons.lang3.StringUtils;
import org.flowable.common.engine.api.delegate.Expression;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.json.JSONObject;

public class SendNotification implements JavaDelegate {

  private static final Logger LOGGER = ApplicationLogger.getLogger(SendNotification.class);

  private Expression notificationCode;

  @Override
  public void execute(DelegateExecution execution) {
    var context = (FlowableApplicationContext) FlowableApplicationContext.of(execution);
    setContext(context);
    var publisher = context.getPublisher();
    var notificationChannel = context.getChannel(NOTIFICATION);
    var entity = execution.getVariable("entity", String.class);
    var data = new JSONObject(entity);

    var notificationCodeValue = notificationCode.getValue(execution);
    var code = notificationCodeValue != null ? notificationCodeValue.toString() : "";
    code = StringUtils.isNotBlank(code) ? code : String.format("%s.%s", getDomain(data), context.getEventType().get());
    var event = new NotificationEvent(code, data);

    LOGGER.info("Sending notification for entity '{}'", getCode(data));
    publisher.publish(notificationChannel, event.toJson());
  }

  private static class NotificationEvent {

    private String notificationCode;

    private JSONObject data;

    public NotificationEvent(String notificationCode, JSONObject data) {
      this.notificationCode = notificationCode;
      this.data = data;
    }

    public JSONObject toJson() {
      var json = new JSONObject();
      json.put("code", notificationCode);
      json.put("data", data);
      return json;
    }
  }
}
