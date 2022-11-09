package com.devit.mscore.history;

import com.devit.mscore.Configuration;
import com.devit.mscore.GitHistory;
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

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ApplicationStarter implements Starter {

  private static final Logger LOGGER = ApplicationLogger.getLogger(ApplicationStarter.class);

  private static final String TOPICS_KEY = "services.%s.topics";

  private Configuration configuration;

  private KafkaMessagingFactory messagingFactory;

  private GitHistory.Builder historyBuilder;

  public ApplicationStarter(String... args) throws ConfigException {
    this(FileConfigurationUtils.load(args));
  }

  public ApplicationStarter(FileConfiguration fileConfiguration) throws ConfigException {
    try {
      var registryFactory = ZookeeperRegistryFactory.of(fileConfiguration);
      var zookeeperRegistry = registryFactory.registry("platformConfig");
      zookeeperRegistry.open();
      configuration = new ZookeeperConfiguration(zookeeperRegistry, fileConfiguration.getServiceName());

      messagingFactory = KafkaMessagingFactory.of(configuration);
      historyBuilder = GitHistory.Builder.of(configuration);
    } catch (RegistryException ex) {
      throw new ConfigException(ex);
    }
  }

  private List<String> getTopicsToListen() throws ConfigException {
    var topicConfigName = String.format(TOPICS_KEY, configuration.getServiceName());
    var topics = configuration.getConfig(topicConfigName)
        .orElseThrow(() -> new ConfigException("No topic provided"));
    return Stream.of(topics.split(",")).collect(Collectors.toList());
  }

  @Override
  public void start() throws ApplicationException {
    var topics = getTopicsToListen();
    if (!topics.isEmpty()) {
      EventListener.of(messagingFactory.subscriber())
          .with(historyBuilder.historyManager())
          .listen(topics.toArray(new String[0]));
    } else {
      LOGGER.warn("No topics to listen to");
    }
  }

  @Override
  public void stop() {
    throw new RuntimeException("Application is stopped");
  }
}
