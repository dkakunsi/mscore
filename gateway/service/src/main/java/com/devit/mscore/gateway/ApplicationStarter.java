package com.devit.mscore.gateway;

import static com.devit.mscore.util.Constants.DOMAIN;

import com.devit.mscore.AuthenticationProvider;
import com.devit.mscore.Configuration;
import com.devit.mscore.ServiceRegistration;
import com.devit.mscore.Starter;
import com.devit.mscore.authentication.JWTAuthenticationProvider;
import com.devit.mscore.configuration.FileConfiguration;
import com.devit.mscore.configuration.FileConfigurationUtils;
import com.devit.mscore.configuration.ZookeeperConfiguration;
import com.devit.mscore.exception.ApplicationException;
import com.devit.mscore.exception.ConfigException;
import com.devit.mscore.exception.RegistryException;
import com.devit.mscore.gateway.api.ApiFactory;
import com.devit.mscore.gateway.service.ResourceService;
import com.devit.mscore.gateway.service.TaskService;
import com.devit.mscore.messaging.kafka.KafkaMessagingFactory;
import com.devit.mscore.registry.ZookeeperRegistryFactory;
import com.devit.mscore.util.DateUtils;
import com.devit.mscore.web.jersey.JerseyClientFactory;

public class ApplicationStarter implements Starter {

  private static final String TIMEZONE = "platform.service.timezone";

  private Configuration configuration;

  private ServiceRegistration serviceRegistration;

  private AuthenticationProvider authenticationProvider;

  private JerseyClientFactory clientFactory;

  private ApiFactory apiFactory;

  public ApplicationStarter(String... args) throws ConfigException {
    this(FileConfigurationUtils.load(args));
  }

  public ApplicationStarter(FileConfiguration fileConfiguration) throws ConfigException {
    try {
      var zookeeperRegistry = ZookeeperRegistryFactory.of(fileConfiguration).registry("platformConfig");
      zookeeperRegistry.open();
      configuration = new ZookeeperConfiguration(zookeeperRegistry, fileConfiguration.getServiceName());
      serviceRegistration = new ServiceRegistration(zookeeperRegistry, configuration);
    } catch (RegistryException ex) {
      throw new ConfigException(ex);
    }

    DateUtils.setZoneId(configuration.getConfig(TIMEZONE).orElse("Asia/Makassar"));
    authenticationProvider = JWTAuthenticationProvider.of(configuration);
    apiFactory = ApiFactory.of(configuration, authenticationProvider);
    clientFactory = JerseyClientFactory.of();
  }

  @Override
  public void start() throws ApplicationException {
    var webClient = clientFactory.client();

    var messagingFactory = KafkaMessagingFactory.of(configuration);
    var domainTopics = messagingFactory.getTopic(DOMAIN).orElseThrow();
    var publisher = messagingFactory.publisher();
    var resourceService = new ResourceService(serviceRegistration, webClient, publisher, domainTopics);
    var taskService = new TaskService(serviceRegistration, webClient);

    apiFactory.add(resourceService);
    apiFactory.add(taskService);

    var server = apiFactory.server();
    server.start();

    serviceRegistration.register(resourceService);
  }

  @Override
  public void stop() {
    throw new RuntimeException("Application is stopped");
  }
}
