package com.devit.mscore.notification;

import com.devit.mscore.ApplicationContext;
import com.devit.mscore.DefaultApplicationContext;
import com.devit.mscore.ResourceManager;
import com.devit.mscore.configuration.FileConfigurationUtils;
import com.devit.mscore.configuration.ZookeeperConfiguration;
import com.devit.mscore.exception.ApplicationException;
import com.devit.mscore.exception.ResourceException;
import com.devit.mscore.messaging.kafka.KafkaMessagingFactory;
import com.devit.mscore.notification.mail.MailNotificationFactory;
import com.devit.mscore.registry.MemoryRegistry;
import com.devit.mscore.registry.ZookeeperRegistryFactory;
import com.devit.mscore.template.pebble.PebbleTemplateFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    private static final String TEMPLATE = "template";

    private static final String NOTIFICATION = "notification";

    public static void main(String[] args) throws ApplicationException {
        try {
            LOGGER.info("Service is starting...");
            start(args);
            LOGGER.info("Service is started!");
        } catch (RuntimeException ex) {
            throw new ApplicationException("Service is fail to start.", ex);
        }
    }

    private static void start(String[] args) throws ApplicationException {
        var context = DefaultApplicationContext.of("starter");
        var fileConfiguration = FileConfigurationUtils.load(args);
        var serviceName = fileConfiguration.getServiceName();

        var zookeeperRegistry = ZookeeperRegistryFactory.of(fileConfiguration).registry(context, "platformConfig");
        zookeeperRegistry.open();
        var configuration = new ZookeeperConfiguration(context, zookeeperRegistry, serviceName);

        var templateRegistry = new MemoryRegistry(TEMPLATE);
        var templateFactory = PebbleTemplateFactory.of(templateRegistry, configuration);
        registerResource(templateFactory, context);

        var template = templateFactory.template();
        var emailNotificationFactory = MailNotificationFactory.of(configuration, templateRegistry, template);
        var mailNotification = emailNotificationFactory.mailNotification(context);

        var kafkaFactory = KafkaMessagingFactory.of(context, configuration);
        var subscriber = kafkaFactory.subscriber();

        var topics = kafkaFactory.getTemplatedTopics(context, NOTIFICATION);
        if (topics.isPresent()) {
            var listener = EventListener.of(subscriber).with(mailNotification);
            listener.listen(topics.get());
        }
    }

    private static void registerResource(ResourceManager resourceManager, ApplicationContext context) {
        LOGGER.info("BreadcrumbId: {}. Register resource: {}.", context.getBreadcrumbId(), resourceManager.getType());
        try {
            resourceManager.registerResources(context);
        } catch (ResourceException ex) {
            LOGGER.warn("BreadcrumbId: {}. Cannot register resource {}.", context.getBreadcrumbId(),
                    resourceManager.getType(), ex);
        }
    }
}
