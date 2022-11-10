package com.devit.mscore.workflow.flowable.delegate;

import static com.devit.mscore.util.AttributeConstants.getCode;
import static com.devit.mscore.util.Constants.CODE;
import static com.devit.mscore.util.Constants.DATA;
import static com.devit.mscore.workflow.flowable.delegate.DelegateUtils.NOTIFICATION;

import com.devit.mscore.ApplicationContext;
import com.devit.mscore.workflow.flowable.delegate.exception.ExpressionNotProvidedException;

import org.apache.commons.lang3.StringUtils;
import org.flowable.common.engine.api.delegate.Expression;
import org.flowable.engine.delegate.DelegateExecution;
import org.json.JSONObject;

public class SendNotification extends ApplicationDelegate {

  Expression code;

  @Override
  public void execute(DelegateExecution execution) {
    var context = initContext(execution);

    var publisher = context.getPublisher();
    var notificationChannel = context.getChannel(NOTIFICATION);
    var data = getEntityObject(execution);
    var notificationCode = getNotificationCodeOrDefault(execution, context, data);
    var event = new NotificationEvent(notificationCode, data);

    logger.info("Sending notification for entity '{}'", getCode(data));
    publisher.publish(notificationChannel, event.toJson());
  }

  private String buildDefaultNotificationCode(DelegateExecution execution, ApplicationContext context,
      JSONObject data) {
    var domain = getDomainVariable(execution);
    return String.format("%s.%s", domain, context.getEventType().get());
  }

  private String getNotificationCodeOrDefault(DelegateExecution execution, ApplicationContext context,
      JSONObject data) {
    try {
      var notificationCode = getValue(execution, code);
      if (StringUtils.isBlank(notificationCode)) {
        return buildDefaultNotificationCode(execution, context, data);
      }
      return notificationCode;
    } catch (ExpressionNotProvidedException ex) {
      return buildDefaultNotificationCode(execution, context, data);
    }
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
      json.put(CODE, notificationCode);
      json.put(DATA, data);
      return json;
    }
  }

  public void setCode(Expression code) {
    this.code = code;
  }
}
