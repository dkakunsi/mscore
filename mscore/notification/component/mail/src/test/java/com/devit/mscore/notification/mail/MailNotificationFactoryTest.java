package com.devit.mscore.notification.mail;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.Optional;

import com.devit.mscore.Configuration;
import com.devit.mscore.Registry;
import com.devit.mscore.exception.ConfigException;

import org.junit.Test;

public class MailNotificationFactoryTest {

  @Test
  public void testCreateMailNotification() throws ConfigException {
    var configuration = mock(Configuration.class);
    doReturn("notification").when(configuration).getServiceName();
    doReturn(Optional.of("host")).when(configuration).getConfig("platform.mail.host");
    doReturn(Optional.of("3025")).when(configuration).getConfig("platform.mail.port");
    doReturn(Optional.of("from@email.com")).when(configuration).getConfig("platform.mail.from");
    doReturn(Optional.of("Subject")).when(configuration).getConfig("services.notification.email.subject");
    doReturn(Optional.of("email,contact")).when(configuration).getConfig("platform.mail.possibleAttributes");
    var registry = mock(Registry.class);
    var template = new StringTemplate();
    var factory = MailNotificationFactory.of(configuration, registry, template);
    var notification = factory.mailNotification();
    assertNotNull(notification);
  }
}
