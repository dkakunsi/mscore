package com.devit.mscore.notification;

import com.devit.mscore.Configuration;
import com.devit.mscore.Logger;
import com.devit.mscore.ResourceManager;
import com.devit.mscore.Starter;
import com.devit.mscore.configuration.FileConfiguration;
import com.devit.mscore.configuration.FileConfigurationUtils;
import com.devit.mscore.configuration.ZookeeperConfiguration;
import com.devit.mscore.exception.ApplicationException;
import com.devit.mscore.exception.ConfigException;
import com.devit.mscore.exception.RegistryException;
import com.devit.mscore.exception.ResourceException;
import com.devit.mscore.logging.ApplicationLogger;
import com.devit.mscore.messaging.kafka.KafkaMessagingFactory;
import com.devit.mscore.notification.mail.MailNotificationFactory;
import com.devit.mscore.registry.MemoryRegistry;
import com.devit.mscore.registry.ZookeeperRegistry;
import com.devit.mscore.registry.ZookeeperRegistryFactory;
import com.devit.mscore.template.pebble.PebbleTemplateFactory;

public class ApplicationStarter implements Starter {

  private static final Logger LOGGER = ApplicationLogger.getLogger(ApplicationStarter.class);

  private static final String TEMPLATE = "template";

  private static final String NOTIFICATION = "notification";

  private ZookeeperRegistry zookeeperRegistry;

  private Configuration configuration;

  private KafkaMessagingFactory messagingFactory;

  public ApplicationStarter(String... args) throws ConfigException {
    this(FileConfigurationUtils.load(args));
  }

  public ApplicationStarter(FileConfiguration fileConfiguration) throws ConfigException {
    try {
      this.zookeeperRegistry = ZookeeperRegistryFactory.of(fileConfiguration).registry("platformConfig");
      zookeeperRegistry.open();
      this.configuration = new ZookeeperConfiguration(zookeeperRegistry, fileConfiguration.getServiceName());
      this.messagingFactory = KafkaMessagingFactory.of(this.configuration);
    } catch (RegistryException ex) {
      throw new ConfigException(ex);
    }
  }

  @Override
  public void start() throws ApplicationException {
    var templateRegistry = new MemoryRegistry(TEMPLATE);
    var templateFactory = PebbleTemplateFactory.of(templateRegistry, configuration);
    registerResource(templateFactory);

    var template = templateFactory.template();
    var emailNotificationFactory = MailNotificationFactory.of(configuration, templateRegistry, template);
    var mailNotification = emailNotificationFactory.mailNotification();

    var subscriber = this.messagingFactory.subscriber();

    var topics = this.messagingFactory.getTemplatedTopics(NOTIFICATION);
    if (topics.isPresent()) {
      var listener = EventListener.of(subscriber).with(mailNotification);
      listener.listen(topics.get());
    }
  }

  private static void registerResource(ResourceManager resourceManager) {
    LOGGER.info("Register resource: {}.", resourceManager.getType());
    try {
      resourceManager.registerResources();
    } catch (ResourceException ex) {
      LOGGER.warn("Cannot register resource {}.", resourceManager.getType(), ex);
    }
  }

  @Override
  public void stop() {
    System.exit(0);
  }
}
