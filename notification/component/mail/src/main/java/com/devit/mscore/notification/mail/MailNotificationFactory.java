package com.devit.mscore.notification.mail;

import com.devit.mscore.Configuration;
import com.devit.mscore.Registry;
import com.devit.mscore.Template;
import com.devit.mscore.exception.ConfigException;

public class MailNotificationFactory {

  private static final String POSSIBLE_ATTRIBUTES = "platform.mail.possibleAttributes";

  private static final String MAIL_HOST = "platform.mail.host";

  private static final String MAIL_PORT = "platform.mail.port";

  private static final String MAIL_USERNAME = "platform.mail.username";

  private static final String MAIL_PASSWORD = "platform.mail.password";

  private static final String MAIL_FROM = "platform.mail.from";

  private static final String MAIL_SUBJECT_TEMPLATE = "services.%s.email.subject";

  private final Configuration configuration;

  private final Registry registry;

  private final Template template;

  private final MailSender sender;

  private MailNotificationFactory(Configuration configuration, Registry registry, Template template) {
    this.configuration = configuration;
    this.registry = registry;
    this.template = template;
    sender = new MailSender();
  }

  public static MailNotificationFactory of(Configuration configuration, Registry registry, Template template) {
    return new MailNotificationFactory(configuration, registry, template);
  }

  public MailNotification mailNotification() throws ConfigException {
    var subjectConfigName = String.format(MAIL_SUBJECT_TEMPLATE, configuration.getServiceName());

    var host = getOrThrow(configuration, MAIL_HOST);
    var port = getOrThrow(configuration, MAIL_PORT);
    var username = getOrBlank(configuration, MAIL_USERNAME);
    var password = getOrBlank(configuration, MAIL_PASSWORD);
    var from = getOrThrow(configuration, MAIL_FROM);
    var subject = getOrThrow(configuration, subjectConfigName);
    var sendInfo = new SendInfo(host, port, username, password, from, subject);

    var possibleAttributes = getOrThrow(configuration, POSSIBLE_ATTRIBUTES);

    return new MailNotification(registry, sender, template, sendInfo)
        .withPossibleAttributes(possibleAttributes);
  }

  private static String getOrThrow(Configuration configuration, String configName) throws ConfigException {
    return configuration.getConfig(configName)
        .orElseThrow(() -> new ConfigException("No configuration value provided for " + configName));
  }

  private static String getOrBlank(Configuration configuration, String configName) throws ConfigException {
    return configuration.getConfig(configName).orElse("");
  }
}
