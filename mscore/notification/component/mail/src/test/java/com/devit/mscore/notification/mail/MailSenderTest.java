package com.devit.mscore.notification.mail;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.MatcherAssert.assertThat;

import jakarta.mail.MessagingException;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetupTest;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class MailSenderTest {

    private MailSender sender;

    private GreenMail smtpServer;

    @Before
    public void setup() {
        this.sender = new MailSender();
        this.smtpServer = new GreenMail(ServerSetupTest.SMTP);
        this.smtpServer.start();
    }

    @After
    public void destroy() {
        this.smtpServer.stop();
    }

    @Test
    public void testSend() throws MessagingException {
        var sendInfo = new SendInfo();
        sendInfo.setHost("localhost");
        sendInfo.setPort("3025");
        sendInfo.setUser("user");
        sendInfo.setPassword("password");
        sendInfo.setFrom("from@email.com");

        this.sender.send(sendInfo, "recipient@email.com", "Subject", "Template name: 123454321");

        var messages = this.smtpServer.getReceivedMessages();
        assertThat(messages.length, is(1));
        assertThat(messages[0].getFrom()[0].toString(), is("from@email.com"));
        assertThat(messages[0].getAllRecipients()[0].toString(), is("recipient@email.com"));
        assertThat(messages[0].getSubject(), is("Subject"));
        var body = GreenMailUtil.getBody(messages[0]);
        assertThat(body, is("Template name: 123454321"));
    }

    @Test
    public void testSend_WithoutAuth() throws MessagingException {
        var sendInfo = new SendInfo();
        sendInfo.setHost("localhost");
        sendInfo.setPort("3025");
        sendInfo.setFrom("from@email.com");

        this.sender.send(sendInfo, "recipient@email.com", "Subject", "Template name: 123454321");

        var messages = this.smtpServer.getReceivedMessages();
        assertThat(messages.length, is(1));
        assertThat(messages[0].getFrom()[0].toString(), is("from@email.com"));
        assertThat(messages[0].getAllRecipients()[0].toString(), is("recipient@email.com"));
        assertThat(messages[0].getSubject(), is("Subject"));
        var body = GreenMailUtil.getBody(messages[0]);
        assertThat(body, is("Template name: 123454321"));
    }
}
