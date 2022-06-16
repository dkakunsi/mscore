package com.devit.mscore.notification.mail;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.devit.mscore.Configuration;
import com.devit.mscore.Registry;

import org.junit.Test;

public class MailNotificationFactoryTest {
    
    @Test
    public void testCreateMailNotification() {
        var configuration = mock(Configuration.class);
        doReturn("host").when(configuration).getConfig(eq("mail.host"));
        doReturn("3025").when(configuration).getConfig(eq("mail.port"));
        doReturn("from@email.com").when(configuration).getConfig(eq("mail.from"));
        doReturn("Subject").when(configuration).getConfig(eq("mail.subject"));
        doReturn("email,contact").when(configuration).getConfig(eq("mail.possibleAttributes"));
        var registry = mock(Registry.class);
        var template = new StringTemplate();
        var factory = MailNotificationFactory.of(configuration, registry, template);
        var notification = factory.mailNotification();
        assertNotNull(notification);
    }
}
