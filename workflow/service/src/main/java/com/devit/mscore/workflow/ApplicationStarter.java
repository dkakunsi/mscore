package com.devit.mscore.workflow;

import static com.devit.mscore.util.Constants.DOMAIN;
import static com.devit.mscore.util.Constants.PROCESS;
import static com.devit.mscore.util.Constants.WORKFLOW;
import static com.devit.mscore.workflow.flowable.delegate.DelegateUtils.NOTIFICATION;

import com.devit.mscore.Configuration;
import com.devit.mscore.DataClient;
import com.devit.mscore.Logger;
import com.devit.mscore.Registry;
import com.devit.mscore.ResourceManager;
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
import com.devit.mscore.workflow.flowable.FlowableWorkflowFactory;
import com.devit.mscore.workflow.flowable.datasource.PgDataSource;
import com.devit.mscore.workflow.flowable.delegate.DelegateUtils;
import com.devit.mscore.workflow.service.WorkflowServiceImpl;

import java.util.Map;

public class ApplicationStarter implements Starter {

  private static final Logger LOGGER = ApplicationLogger.getLogger(ApplicationStarter.class);

  private static final String WORKFLOW_DOMAIN = "services.%s.domain.workflow";

  private static final String TIMEZONE = "platform.service.timezone";

  // TODO: use core constants
  private static final String TASK = "task";

  private String serviceName;

  private Registry zookeeperRegistry;

  private Registry workflowRegistry;

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
    serviceName = fileConfiguration.getServiceName();
    try {
      workflowRegistry = new MemoryRegistry(WORKFLOW);
      zookeeperRegistry = ZookeeperRegistryFactory.of(fileConfiguration).registry("platformCOnfig");
      zookeeperRegistry.open();
      configuration = new ZookeeperConfiguration(zookeeperRegistry, serviceName);

      DateUtils.setZoneId(configuration.getConfig(TIMEZONE).orElse("Asia/Makassar"));

      messagingFactory = KafkaMessagingFactory.of(configuration);
      authenticationProvider = JWTAuthenticationProvider.of(configuration);
      webClient = JerseyClientFactory.of().client();
      apiFactory = ApiFactory.of(configuration, authenticationProvider);
      var workflowDataSource = new PgDataSource(configuration);
      workflowFactory = FlowableWorkflowFactory.of(configuration, workflowRegistry, workflowDataSource);
      serviceRegistration = new ServiceRegistration(zookeeperRegistry, configuration);
    } catch (RegistryException ex) {
      throw new ConfigException(ex);
    }
  }

  @Override
  public void start() throws ApplicationException {
    registerResource(workflowFactory);

    // Create publisher
    var publisher = messagingFactory.publisher();

    var notificationChannel = messagingFactory.getTemplatedTopics(NOTIFICATION).orElse(new String[] { "" });
    var domainChannel = messagingFactory.getTemplatedTopics(DOMAIN).orElse(new String[] { "" });
    Map<String, String> channels = Map.of(NOTIFICATION, notificationChannel[0], DOMAIN, domainChannel[0]);

    var workflowDomain = getWorkflowDomain(WORKFLOW_DOMAIN);
    var dataClient = new DataClient(webClient, serviceRegistration, workflowDomain);

    var definitionRepository = workflowFactory.definitionRepository();
    var instanceRepository = workflowFactory.instanceRepository();
    var taskRepository = workflowFactory.taskRepository();
    var workflowService = new WorkflowServiceImpl(workflowRegistry, publisher, domainChannel[0], definitionRepository,
        instanceRepository, taskRepository);

    for (var definition : workflowFactory.getDefinitions()) {
      workflowService.deployDefinition(definition);
    }

    // Start service
    var server = apiFactory.addService(workflowService).server();
    server.start();

    serviceRegistration.register(PROCESS);
    serviceRegistration.register(TASK);

    // This resources will be used by workflow delegates.
    DelegateUtils.setDataClient(dataClient);
    DelegateUtils.setPublisher(publisher);
    DelegateUtils.setChannels(channels);
    DelegateUtils.setConfiguration(configuration);
  }

  private String getWorkflowDomain(String configTemplate) throws ConfigException {
    var configName = String.format(configTemplate, serviceName);
    return configuration.getConfig(configName)
        .orElseThrow(() -> new ConfigException(configName + " is not configured"));
  }

  private void registerResource(ResourceManager resourceManager) {
    LOGGER.info("Register resource: '{}'", resourceManager.getType());
    try {
      resourceManager.registerResources();
    } catch (ResourceException ex) {
      LOGGER.warn("Cannot register resource '{}'", resourceManager.getType(), ex);
    }
  }

  @Override
  public void stop() {
    throw new RuntimeException("Application is stopped");
  }
}
