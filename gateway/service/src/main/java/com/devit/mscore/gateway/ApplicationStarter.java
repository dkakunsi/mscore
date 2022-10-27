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
import com.devit.mscore.gateway.service.WorkflowService;
import com.devit.mscore.messaging.kafka.KafkaMessagingFactory;
import com.devit.mscore.registry.ZookeeperRegistryFactory;
import com.devit.mscore.util.DateUtils;
import com.devit.mscore.web.jersey.JerseyClientFactory;

import org.apache.commons.lang3.BooleanUtils;

public class ApplicationStarter implements Starter {

  private static final String WORKFLOW_SERVICES = "services.%s.workflow.enabled";

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
    var messagingFactory = KafkaMessagingFactory.of(this.configuration);
    var useWorkflow = isUseWorkflow();
    var workflowService = new WorkflowService(this.serviceRegistration, webClient);
    var resourceService = new ResourceService(this.serviceRegistration, webClient, workflowService, useWorkflow);
    var publisher = messagingFactory.publisher(DOMAIN);
    var eventEmitter = new EventEmitter(publisher);

    this.apiFactory.add(resourceService);
    this.apiFactory.add(eventEmitter);

    if (BooleanUtils.isTrue(useWorkflow)) {
      this.apiFactory.add(workflowService);
    }

    var server = this.apiFactory.server();
    server.start();

    this.serviceRegistration.open();
    this.serviceRegistration.register(resourceService);
    this.serviceRegistration.register(workflowService);
  }

  private boolean isUseWorkflow() throws ConfigException {
    var configName = String.format(WORKFLOW_SERVICES, this.configuration.getServiceName());
    return Boolean.valueOf(this.configuration.getConfig(configName).orElse("false"));
  }

  @Override
  public void stop() {
    throw new RuntimeException("Application is stopped.");
  }
}
