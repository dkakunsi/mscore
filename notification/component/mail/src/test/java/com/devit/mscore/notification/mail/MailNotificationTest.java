package com.devit.mscore.notification.mail;

import static com.devit.mscore.util.Constants.DOMAIN;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.devit.mscore.ApplicationContext;
import com.devit.mscore.Registry;
import com.devit.mscore.Template;
import com.devit.mscore.exception.NotificationException;
import com.devit.mscore.exception.RegistryException;

import java.util.Optional;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import jakarta.mail.MessagingException;

public class MailNotificationTest {

  private static final String TEMPLATE_NAME = "domain.create";

  private Registry registry;

  private MailSender sender;

  private Template template;

  private ApplicationContext context;

  private MailNotification notification;

  private SendInfo sendInfo;

  @Before
  public void setup() {
    registry = mock(Registry.class);
    context = mock(ApplicationContext.class);
    doReturn("breadcrumbId").when(context).getBreadcrumbId();
    doReturn(Optional.of("create")).when(context).getEventType();
    sender = mock(MailSender.class);
    template = new StringTemplate();
    sendInfo = new SendInfo("localhost", "3025", "user", "password", "from@email.com", "Subject");
    notification = new MailNotification(registry, sender, template, sendInfo)
        .withPossibleAttributes("email");
  }

  @Test
  public void testSend() throws RegistryException, NotificationException, MessagingException {
    var template = new JSONObject("{\"content\":\"Template name: %s\"}");
    doReturn(template.toString()).when(registry).get(TEMPLATE_NAME);

    var json = new JSONObject();
    json.put(DOMAIN, DOMAIN);
    json.put("email", "recipient@email.com");
    json.put("id", "123454321");
    json.put("name", "Name");

    try (MockedStatic<ApplicationContext> utilities = Mockito.mockStatic(ApplicationContext.class)) {
      utilities.when(() -> ApplicationContext.getContext()).thenReturn(context);

      notification.send("", json);
      verify(sender).send(sendInfo, "recipient@email.com", "Subject | Name",
          "Template name: 123454321");
    }
  }

  @Test
  public void testSend_NoRecipientEmail() throws NotificationException, MessagingException {
    try (MockedStatic<ApplicationContext> utilities = Mockito.mockStatic(ApplicationContext.class)) {
      utilities.when(() -> ApplicationContext.getContext()).thenReturn(context);

      notification.send("", new JSONObject());
      verify(sender, never()).send(any(), anyString(), anyString(), anyString());
    }
  }

  // @Test
  public void testSend_NoTemplateInEntity() throws NotificationException {
    var json = new JSONObject();
    json.put("email", "recipient@email.com");

    try (MockedStatic<ApplicationContext> utilities = Mockito.mockStatic(ApplicationContext.class)) {
      utilities.when(() -> ApplicationContext.getContext()).thenReturn(context);

      var ex = assertThrows(NotificationException.class, () -> notification.send(TEMPLATE_NAME, json));
      assertThat(ex.getMessage(), is("No email template found for this request"));
    }
  }

  @Test
  public void testSend_RegistryException() throws NotificationException, RegistryException {
    doThrow(new RegistryException("")).when(registry).get(TEMPLATE_NAME);

    var json = new JSONObject();
    json.put(DOMAIN, DOMAIN);
    json.put("email", "recipient@email.com");

    try (MockedStatic<ApplicationContext> utilities = Mockito.mockStatic(ApplicationContext.class)) {
      utilities.when(() -> ApplicationContext.getContext()).thenReturn(context);

      var ex = assertThrows(NotificationException.class, () -> notification.send(TEMPLATE_NAME, json));
      assertThat(ex.getMessage(), is("Cannot load email template"));
      assertThat(ex.getCause(), instanceOf(RegistryException.class));
    }
  }

  @Test
  public void testSend_MessagingException() throws NotificationException, RegistryException, MessagingException {
    doThrow(new MessagingException()).when(sender).send(any(), eq("recipient@email.com"), eq("Subject | Name"),
        eq("Template name: 123454321"));

    var template = new JSONObject("{\"content\":\"Template name: %s\"}");
    doReturn(template.toString()).when(registry).get(TEMPLATE_NAME);

    var json = new JSONObject();
    json.put(DOMAIN, DOMAIN);
    json.put("email", "recipient@email.com");
    json.put("id", "123454321");
    json.put("name", "Name");

    try (MockedStatic<ApplicationContext> utilities = Mockito.mockStatic(ApplicationContext.class)) {
      utilities.when(() -> ApplicationContext.getContext()).thenReturn(context);

      var ex = assertThrows(NotificationException.class, () -> notification.send(TEMPLATE_NAME, json));
      assertThat(ex.getMessage(), is("Cannot send email"));
      assertThat(ex.getCause(), instanceOf(MessagingException.class));
    }

  }
}
