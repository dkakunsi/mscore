package com.devit.mscore.notification.mail;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetupTest;

import jakarta.mail.MessagingException;

public class MailSenderTest {

  private MailSender sender;

  private GreenMail smtpServer;

  @Before
  public void setup() {
    sender = new MailSender();
    smtpServer = new GreenMail(ServerSetupTest.SMTP);
    smtpServer.start();
  }

  @After
  public void destroy() {
    smtpServer.stop();
  }

  @Test
  public void testSend() throws MessagingException {
    var sendInfo = new SendInfo("localhost", "3025", "user", "password", "from@email.com", "subject");
    sender.send(sendInfo, "recipient@email.com", "Subject", "Template name: 123454321");

    var messages = smtpServer.getReceivedMessages();
    assertThat(messages.length, is(1));
    assertThat(messages[0].getFrom()[0].toString(), is("from@email.com"));
    assertThat(messages[0].getAllRecipients()[0].toString(), is("recipient@email.com"));
    assertThat(messages[0].getSubject(), is("Subject"));
    var body = GreenMailUtil.getBody(messages[0]);
    assertThat(body, is("Template name: 123454321"));
  }

  @Test
  public void testSend_WithoutAuth() throws MessagingException {
    var sendInfo = new SendInfo("localhost", "3025", "from@email.com", "subject");
    sender.send(sendInfo, "recipient@email.com", "Subject", "Template name: 123454321");

    var messages = smtpServer.getReceivedMessages();
    assertThat(messages.length, is(1));
    assertThat(messages[0].getFrom()[0].toString(), is("from@email.com"));
    assertThat(messages[0].getAllRecipients()[0].toString(), is("recipient@email.com"));
    assertThat(messages[0].getSubject(), is("Subject"));
    var body = GreenMailUtil.getBody(messages[0]);
    assertThat(body, is("Template name: 123454321"));
  }
}
