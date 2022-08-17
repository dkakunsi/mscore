package com.devit.mscore.notification.mail;

import static jakarta.mail.Message.RecipientType.TO;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import org.apache.commons.lang3.StringUtils;

public class MailSender {

  public void send(SendInfo sendInfo, String to, String emailSubject, String text) throws MessagingException {
    var properties = System.getProperties();
    properties.setProperty("mail.smtp.host", sendInfo.getHost());
    properties.setProperty("mail.smtp.port", sendInfo.getPort());

    Session session;
    if (StringUtils.isNotBlank(sendInfo.getUser()) && StringUtils.isNotBlank(sendInfo.getPassword())) {
      var auth = new Authenticator() {
        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
          return new PasswordAuthentication(sendInfo.getUser(), sendInfo.getPassword());
        }
      };

      session = Session.getInstance(properties, auth);
    } else {
      session = Session.getInstance(properties);
    }

    var message = createMessage(session, sendInfo.getFrom(), to, emailSubject, text);
    Transport.send(message);
  }

  private Message createMessage(Session session, String from, String to, String subject, String text)
      throws MessagingException {
    var message = new MimeMessage(session);
    message.setFrom(new InternetAddress(from));
    message.addRecipient(TO, new InternetAddress(to));
    message.setSubject(subject);
    message.setText(text);
    return message;
  }

}
