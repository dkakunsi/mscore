package com.devit.mscore.notification;

import static com.devit.mscore.ApplicationContext.setContext;

import com.devit.mscore.DefaultApplicationContext;
import com.devit.mscore.Logger;
import com.devit.mscore.ResourceManager;
import com.devit.mscore.configuration.FileConfigurationUtils;
import com.devit.mscore.configuration.ZookeeperConfiguration;
import com.devit.mscore.exception.ApplicationException;
import com.devit.mscore.exception.ResourceException;
import com.devit.mscore.logging.ApplicationLogger;
import com.devit.mscore.messaging.kafka.KafkaMessagingFactory;
import com.devit.mscore.notification.mail.MailNotificationFactory;
import com.devit.mscore.registry.MemoryRegistry;
import com.devit.mscore.registry.ZookeeperRegistryFactory;
import com.devit.mscore.template.pebble.PebbleTemplateFactory;

public class Main {

    private static final Logger LOGGER = ApplicationLogger.getLogger(Main.class);

    private static final String TEMPLATE = "template";

    private static final String NOTIFICATION = "notification";

    public static void main(String[] args) throws ApplicationException {
        setContext(DefaultApplicationContext.of("starter"));
        try {
            LOGGER.info("Service is starting...");
            start(args);
            LOGGER.info("Service is started!");
        } catch (RuntimeException ex) {
            throw new ApplicationException("Service is fail to start.", ex);
        }
    }

    // TODO: use application starter
    private static void start(String[] args) throws ApplicationException {
        var fileConfiguration = FileConfigurationUtils.load(args);
        var serviceName = fileConfiguration.getServiceName();

        var zookeeperRegistry = ZookeeperRegistryFactory.of(fileConfiguration).registry("platformConfig");
        zookeeperRegistry.open();
        var configuration = new ZookeeperConfiguration(zookeeperRegistry, serviceName);

        var templateRegistry = new MemoryRegistry(TEMPLATE);
        var templateFactory = PebbleTemplateFactory.of(templateRegistry, configuration);
        registerResource(templateFactory);

        var template = templateFactory.template();
        var emailNotificationFactory = MailNotificationFactory.of(configuration, templateRegistry, template);
        var mailNotification = emailNotificationFactory.mailNotification();

        var kafkaFactory = KafkaMessagingFactory.of(configuration);
        var subscriber = kafkaFactory.subscriber();

        var topics = kafkaFactory.getTemplatedTopics(NOTIFICATION);
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
}
