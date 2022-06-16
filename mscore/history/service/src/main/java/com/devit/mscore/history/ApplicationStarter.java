package com.devit.mscore.history;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.devit.mscore.ApplicationContext;
import com.devit.mscore.Configuration;
import com.devit.mscore.DefaultApplicationContext;
import com.devit.mscore.GitHistoryFactory;
import com.devit.mscore.Starter;
import com.devit.mscore.configuration.FileConfiguration;
import com.devit.mscore.configuration.FileConfigurationUtils;
import com.devit.mscore.configuration.ZookeeperConfiguration;
import com.devit.mscore.exception.ApplicationException;
import com.devit.mscore.exception.ConfigException;
import com.devit.mscore.exception.RegistryException;
import com.devit.mscore.messaging.kafka.KafkaMessagingFactory;
import com.devit.mscore.registry.ZookeeperRegistryFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApplicationStarter implements Starter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationStarter.class);

    private static final String TOPICS = "services.%s.topics";

    private Configuration configuration;

    private ApplicationContext context;

    private List<String> topics;

    public ApplicationStarter(String[] args) throws ConfigException {
        this(FileConfigurationUtils.load(args));
    }

    public ApplicationStarter(FileConfiguration fileConfiguration) throws ConfigException {
        this.context = DefaultApplicationContext.of("starter");

        try {
            var registryFactory = ZookeeperRegistryFactory.of(fileConfiguration);
            var zookeeperRegistry = registryFactory.registry(context, "platformConfig");
            zookeeperRegistry.open();
            this.configuration = new ZookeeperConfiguration(context, zookeeperRegistry,
                    fileConfiguration.getServiceName());
            this.topics = getTopicsToListen(context, configuration);
        } catch (RegistryException ex) {
            throw new ConfigException(ex);
        }
    }

    static List<String> getTopicsToListen(ApplicationContext context, Configuration configuration)
            throws ConfigException {
        var topicConfigName = String.format(TOPICS, configuration.getServiceName());
        var topics = configuration.getConfig(context, topicConfigName)
                .orElseThrow(() -> new ConfigException("No topic provided."));
        return Stream.of(topics.split(",")).collect(Collectors.toList());
    }

    @Override
    public void start() throws ApplicationException {

        var messagingFactory = KafkaMessagingFactory.of(this.context, this.configuration);
        var subscriber = messagingFactory.subscriber();
        var historyFactory = GitHistoryFactory.of(this.context, this.configuration);

        if (this.topics.isEmpty()) {
            LOGGER.warn("BreadcrumbId: {}. No topics to listen to", this.context.getBreadcrumbId());
        } else {
            var listener = EventListener.of(subscriber).with(historyFactory.historyManager(this.context));
            listener.listen(this.topics.toArray(new String[0]));
        }
    }

    @Override
    public void stop() {
        System.exit(0);
    }
}
