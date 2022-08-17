package com.devit.mscore.workflow;

import static com.devit.mscore.util.Utils.WORKFLOW;
import static com.devit.mscore.workflow.flowable.DelegateUtils.NOTIFICATION;

import java.util.HashMap;

import com.devit.mscore.Configuration;
import com.devit.mscore.Logger;
import com.devit.mscore.Publisher;
import com.devit.mscore.Registry;
import com.devit.mscore.ResourceManager;
import com.devit.mscore.Service;
import com.devit.mscore.ServiceRegistration;
import com.devit.mscore.Starter;
import com.devit.mscore.authentication.JWTAuthenticationProvider;
import com.devit.mscore.configuration.FileConfiguration;
import com.devit.mscore.configuration.FileConfigurationUtils;
import com.devit.mscore.configuration.ZookeeperConfiguration;
import com.devit.mscore.exception.ApplicationException;
import com.devit.mscore.exception.ConfigException;
import com.devit.mscore.exception.RegistryException;
import com.devit.mscore.exception.ResourceException;
import com.devit.mscore.logging.ApplicationLogger;
import com.devit.mscore.messaging.kafka.KafkaMessagingFactory;
import com.devit.mscore.registry.MemoryRegistry;
import com.devit.mscore.registry.ZookeeperRegistryFactory;
import com.devit.mscore.util.DateUtils;
import com.devit.mscore.web.Client;
import com.devit.mscore.web.jersey.JerseyClientFactory;
import com.devit.mscore.workflow.api.ApiFactory;
import com.devit.mscore.workflow.flowable.DataClient;
import com.devit.mscore.workflow.flowable.DelegateUtils;
import com.devit.mscore.workflow.flowable.FlowableWorkflowFactory;

public class ApplicationStarter implements Starter {

  private static final Logger LOGGER = ApplicationLogger.getLogger(ApplicationStarter.class);

  private static final String WORKFLOW_DOMAIN = "services.%s.domain.workflow";

  private static final String TASK_DOMAIN = "services.%s.domain.task";

  private static final String TIMEZONE = "platform.service.timezone";

  private String serviceName;

  private Registry zookeeperRegistry;

  private Configuration configuration;

  private KafkaMessagingFactory messagingFactory;

  private FlowableWorkflowFactory workflowFactory;

  private JWTAuthenticationProvider authenticationProvider;

  private Client webClient;

  private ApiFactory apiFactory;

  private ServiceRegistration serviceRegistration;

  public ApplicationStarter(String... args) throws ConfigException {
    this(FileConfigurationUtils.load(args));
  }

  public ApplicationStarter(FileConfiguration fileConfiguration) throws ConfigException {
    this.serviceName = fileConfiguration.getServiceName();
    try {
      this.zookeeperRegistry = ZookeeperRegistryFactory.of(fileConfiguration).registry("platformCOnfig");
      zookeeperRegistry.open();
      this.configuration = new ZookeeperConfiguration(zookeeperRegistry, serviceName);

      DateUtils.setZoneId(this.configuration.getConfig(TIMEZONE).orElse("Asia/Makassar"));

      this.messagingFactory = KafkaMessagingFactory.of(this.configuration);
      this.authenticationProvider = JWTAuthenticationProvider.of(this.configuration);
      this.webClient = JerseyClientFactory.of().client();
      this.apiFactory = ApiFactory.of(this.configuration, this.authenticationProvider);
      this.workflowFactory = FlowableWorkflowFactory.of(this.configuration, new MemoryRegistry(WORKFLOW));
      this.serviceRegistration = new ServiceRegistration(this.zookeeperRegistry, this.configuration);
    } catch (RegistryException ex) {
      throw new ConfigException(ex);
    }
  }

  @Override
  public void start() throws ApplicationException {
    registerResource(this.workflowFactory);

    // Create publisher
    var publishers = new HashMap<String, Publisher>();
    publishers.put(NOTIFICATION, this.messagingFactory.publisher(NOTIFICATION));

    var workflowDomain = getWorkflowDomain(WORKFLOW_DOMAIN);
    var taskDomain = getWorkflowDomain(TASK_DOMAIN);
    var dataClient = new DataClient(this.webClient, this.serviceRegistration, workflowDomain, taskDomain);

    var workflowProcess = this.workflowFactory.workflowProcess(dataClient);
    workflowProcess.start();

    for (var definition : this.workflowFactory.getDefinitions()) {
      workflowProcess.deployDefinition(definition);
    }

    // Start service
    var server = this.apiFactory.addService((Service) workflowProcess).server();
    server.start();

    this.serviceRegistration.register((Service) workflowProcess);

    // This resources will be used by workflow delegates.
    DelegateUtils.setDataClient(dataClient);
    DelegateUtils.setPublishers(publishers);
    DelegateUtils.setConfiguration(this.configuration);
  }

  private String getWorkflowDomain(String configTemplate) throws ConfigException {
    var configName = String.format(configTemplate, this.serviceName);
    return this.configuration.getConfig(configName)
        .orElseThrow(() -> new ConfigException(configName + " is not configured"));
  }

  private void registerResource(ResourceManager resourceManager) {
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
