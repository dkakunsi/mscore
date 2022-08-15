package com.devit.mscore.history;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.devit.mscore.Configuration;
import com.devit.mscore.GitHistoryFactory;
import com.devit.mscore.Logger;
import com.devit.mscore.Starter;
import com.devit.mscore.configuration.FileConfiguration;
import com.devit.mscore.configuration.FileConfigurationUtils;
import com.devit.mscore.configuration.ZookeeperConfiguration;
import com.devit.mscore.exception.ApplicationException;
import com.devit.mscore.exception.ConfigException;
import com.devit.mscore.exception.RegistryException;
import com.devit.mscore.logging.ApplicationLogger;
import com.devit.mscore.messaging.kafka.KafkaMessagingFactory;
import com.devit.mscore.registry.ZookeeperRegistryFactory;

public class ApplicationStarter implements Starter {

    private static final Logger LOGGER = ApplicationLogger.getLogger(ApplicationStarter.class);

    private static final String TOPICS_KEY = "services.%s.topics";

    private Configuration configuration;

    private List<String> topics;

    public ApplicationStarter(String[] args) throws ConfigException {
        this(FileConfigurationUtils.load(args));
    }

    public ApplicationStarter(FileConfiguration fileConfiguration) throws ConfigException {
        try {
            var registryFactory = ZookeeperRegistryFactory.of(fileConfiguration);
            var zookeeperRegistry = registryFactory.registry("platformConfig");
            zookeeperRegistry.open();
            this.configuration = new ZookeeperConfiguration(zookeeperRegistry, fileConfiguration.getServiceName());
            this.topics = getTopicsToListen(configuration);
        } catch (RegistryException ex) {
            throw new ConfigException(ex);
        }
    }

    static List<String> getTopicsToListen(Configuration configuration)
            throws ConfigException {
        var topicConfigName = String.format(TOPICS_KEY, configuration.getServiceName());
        var topics = configuration.getConfig(topicConfigName)
                .orElseThrow(() -> new ConfigException("No topic provided."));
        return Stream.of(topics.split(",")).collect(Collectors.toList());
    }

    @Override
    public void start() throws ApplicationException {
        var messagingFactory = KafkaMessagingFactory.of(this.configuration);
        var subscriber = messagingFactory.subscriber();
        var historyFactory = GitHistoryFactory.of(this.configuration);

        if (this.topics.isEmpty()) {
            LOGGER.warn("No topics to listen to");
        } else {
            var listener = EventListener.of(subscriber).with(historyFactory.historyManager());
            listener.listen(this.topics.toArray(new String[0]));
        }
    }

    @Override
    public void stop() {
        System.exit(0);
    }
}
