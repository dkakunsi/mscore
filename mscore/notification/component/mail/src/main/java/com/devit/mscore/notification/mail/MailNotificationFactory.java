package com.devit.mscore.notification.mail;

import com.devit.mscore.Configuration;
import com.devit.mscore.Registry;
import com.devit.mscore.Template;
import com.devit.mscore.exception.ConfigException;

public class MailNotificationFactory {

  private static final String POSSIBLE_ATTRIBUTES = "platform.mail.possibleAttributes";

  private final Configuration configuration;

  private final Registry registry;

  private final Template template;

  private final MailSender sender;

  private MailNotificationFactory(Configuration configuration, Registry registry, Template template) {
    this.configuration = configuration;
    this.registry = registry;
    this.template = template;
    this.sender = new MailSender();
  }

  public static MailNotificationFactory of(Configuration configuration, Registry registry, Template template) {
    return new MailNotificationFactory(configuration, registry, template);
  }

  public MailNotification mailNotification() throws ConfigException {
    var sendInfo = new SendInfo(this.configuration);
    var possibleAttributes = this.configuration.getConfig(POSSIBLE_ATTRIBUTES)
        .orElseThrow(() -> new ConfigException("No possible attribute is configured"));
    return new MailNotification(this.registry, this.sender, this.template, sendInfo)
        .withPossibleAttributes(possibleAttributes);
  }
}
