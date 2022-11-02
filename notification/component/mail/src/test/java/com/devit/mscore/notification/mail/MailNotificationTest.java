package com.devit.mscore.notification.mail;

import static com.devit.mscore.util.AttributeConstants.DOMAIN;
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
    this.registry = mock(Registry.class);
    this.context = mock(ApplicationContext.class);
    doReturn("breadcrumbId").when(this.context).getBreadcrumbId();
    doReturn(Optional.of("create")).when(this.context).getEventType();
    this.sender = mock(MailSender.class);
    this.template = new StringTemplate();

    this.sendInfo = new SendInfo();
    this.sendInfo.setHost("localhost");
    this.sendInfo.setFrom("from@email.com");
    this.sendInfo.setPort("3025");
    this.sendInfo.setUser("user");
    this.sendInfo.setPassword("password");
    this.sendInfo.setSubject("Subject");

    this.notification = new MailNotification(this.registry, this.sender, this.template, this.sendInfo)
        .withPossibleAttributes("email");
  }

  @Test
  public void testSend() throws RegistryException, NotificationException, MessagingException {
    var template = new JSONObject("{\"content\":\"Template name: %s\"}");
    doReturn(template.toString()).when(this.registry).get(TEMPLATE_NAME);

    var json = new JSONObject();
    json.put(DOMAIN, DOMAIN);
    json.put("email", "recipient@email.com");
    json.put("id", "123454321");
    json.put("name", "Name");

    try (MockedStatic<ApplicationContext> utilities = Mockito.mockStatic(ApplicationContext.class)) {
      utilities.when(() -> ApplicationContext.getContext()).thenReturn(this.context);

      this.notification.send("", json);
      verify(this.sender).send(this.sendInfo, "recipient@email.com", "Subject | Name",
          "Template name: 123454321");
    }
  }

  @Test
  public void testSend_NoRecipientEmail() throws NotificationException, MessagingException {
    try (MockedStatic<ApplicationContext> utilities = Mockito.mockStatic(ApplicationContext.class)) {
      utilities.when(() -> ApplicationContext.getContext()).thenReturn(this.context);

      this.notification.send("", new JSONObject());
      verify(this.sender, never()).send(any(), anyString(), anyString(), anyString());
    }
  }

  // @Test
  public void testSend_NoTemplateInEntity() throws NotificationException {
    var json = new JSONObject();
    json.put("email", "recipient@email.com");

    try (MockedStatic<ApplicationContext> utilities = Mockito.mockStatic(ApplicationContext.class)) {
      utilities.when(() -> ApplicationContext.getContext()).thenReturn(this.context);

      var ex = assertThrows(NotificationException.class, () -> this.notification.send(TEMPLATE_NAME, json));
      assertThat(ex.getMessage(), is("No email template found for this request"));
    }
  }

  @Test
  public void testSend_RegistryException() throws NotificationException, RegistryException {
    doThrow(new RegistryException("")).when(this.registry).get(TEMPLATE_NAME);

    var json = new JSONObject();
    json.put(DOMAIN, DOMAIN);
    json.put("email", "recipient@email.com");

    try (MockedStatic<ApplicationContext> utilities = Mockito.mockStatic(ApplicationContext.class)) {
      utilities.when(() -> ApplicationContext.getContext()).thenReturn(this.context);

      var ex = assertThrows(NotificationException.class, () -> this.notification.send(TEMPLATE_NAME, json));
      assertThat(ex.getMessage(), is("Cannot load email template"));
      assertThat(ex.getCause(), instanceOf(RegistryException.class));
    }
  }

  @Test
  public void testSend_MessagingException() throws NotificationException, RegistryException, MessagingException {
    doThrow(new MessagingException()).when(this.sender).send(any(), eq("recipient@email.com"), eq("Subject | Name"),
        eq("Template name: 123454321"));

    var template = new JSONObject("{\"content\":\"Template name: %s\"}");
    doReturn(template.toString()).when(this.registry).get(TEMPLATE_NAME);

    var json = new JSONObject();
    json.put(DOMAIN, DOMAIN);
    json.put("email", "recipient@email.com");
    json.put("id", "123454321");
    json.put("name", "Name");

    try (MockedStatic<ApplicationContext> utilities = Mockito.mockStatic(ApplicationContext.class)) {
      utilities.when(() -> ApplicationContext.getContext()).thenReturn(this.context);

      var ex = assertThrows(NotificationException.class, () -> this.notification.send(TEMPLATE_NAME, json));
      assertThat(ex.getMessage(), is("Cannot send email"));
      assertThat(ex.getCause(), instanceOf(MessagingException.class));
    }

  }
}
