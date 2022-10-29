package com.devit.mscore.gateway;

import static com.devit.mscore.util.AttributeConstants.DOMAIN;

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
import com.devit.mscore.gateway.service.EventEmitter;
import com.devit.mscore.gateway.service.ResourceService;
import com.devit.mscore.messaging.kafka.KafkaMessagingFactory;
import com.devit.mscore.registry.ZookeeperRegistryFactory;
import com.devit.mscore.util.DateUtils;
import com.devit.mscore.web.jersey.JerseyClientFactory;

public class ApplicationStarter implements Starter {

  private static final String TIMEZONE = "platform.service.timezone";

  // TODO: use constant from api package
  private static final java.lang.String WORKFLOW = "workflow";

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
      this.configuration = new ZookeeperConfiguration(zookeeperRegistry, fileConfiguration.getServiceName());
      this.serviceRegistration = new ServiceRegistration(zookeeperRegistry, configuration);
    } catch (RegistryException ex) {
      throw new ConfigException(ex);
    }

    DateUtils.setZoneId(this.configuration.getConfig(TIMEZONE).orElse("Asia/Makassar"));
    this.authenticationProvider = JWTAuthenticationProvider.of(configuration);
    this.apiFactory = ApiFactory.of(this.configuration, this.authenticationProvider);
    this.clientFactory = JerseyClientFactory.of();
  }

  @Override
  public void start() throws ApplicationException {
    var webClient = this.clientFactory.client();
    var resourceService = new ResourceService(this.serviceRegistration, webClient);

    var messagingFactory = KafkaMessagingFactory.of(this.configuration);
    var domainTopics = messagingFactory.getTopics(DOMAIN).orElse(new String[] {""});
    var workflowTopics = messagingFactory.getTopics(WORKFLOW).orElse(new String[] {""});
    var publisher = messagingFactory.publisher();
    var eventEmitter = new EventEmitter(publisher, domainTopics[0], workflowTopics[0]);

    this.apiFactory.add(resourceService);
    this.apiFactory.add(eventEmitter);

    var server = this.apiFactory.server();
    server.start();

    this.serviceRegistration.open();
    this.serviceRegistration.register(resourceService);
    this.serviceRegistration.register(eventEmitter);
  }

  @Override
  public void stop() {
    throw new RuntimeException("Application is stopped");
  }
}
