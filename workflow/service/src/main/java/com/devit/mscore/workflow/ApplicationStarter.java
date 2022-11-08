package com.devit.mscore.workflow;

import static com.devit.mscore.util.AttributeConstants.DOMAIN;
import static com.devit.mscore.util.Utils.WORKFLOW;
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
    this.serviceName = fileConfiguration.getServiceName();
    try {
      this.workflowRegistry = new MemoryRegistry(WORKFLOW);
      this.zookeeperRegistry = ZookeeperRegistryFactory.of(fileConfiguration).registry("platformCOnfig");
      zookeeperRegistry.open();
      this.configuration = new ZookeeperConfiguration(zookeeperRegistry, serviceName);

      DateUtils.setZoneId(this.configuration.getConfig(TIMEZONE).orElse("Asia/Makassar"));

      this.messagingFactory = KafkaMessagingFactory.of(this.configuration);
      this.authenticationProvider = JWTAuthenticationProvider.of(this.configuration);
      this.webClient = JerseyClientFactory.of().client();
      this.apiFactory = ApiFactory.of(this.configuration, this.authenticationProvider);
      var workflowDataSource = new PgDataSource(this.configuration);
      this.workflowFactory = FlowableWorkflowFactory.of(this.configuration, this.workflowRegistry, workflowDataSource);
      this.serviceRegistration = new ServiceRegistration(this.zookeeperRegistry, this.configuration);
    } catch (RegistryException ex) {
      throw new ConfigException(ex);
    }
  }

  @Override
  public void start() throws ApplicationException {
    registerResource(this.workflowFactory);

    // Create publisher
    var publisher = this.messagingFactory.publisher();

    var notificationChannel = messagingFactory.getTemplatedTopics(NOTIFICATION).orElse(new String[] { "" });
    var domainChannel = messagingFactory.getTemplatedTopics(DOMAIN).orElse(new String[] { "" });
    Map<String, String> channels = Map.of(NOTIFICATION, notificationChannel[0], DOMAIN, domainChannel[0]);

    var workflowDomain = getWorkflowDomain(WORKFLOW_DOMAIN);
    var dataClient = new DataClient(this.webClient, this.serviceRegistration, workflowDomain);

    var definitionRepository = this.workflowFactory.definitionRepository();
    var instanceRepository = this.workflowFactory.instanceRepository();
    var taskRepository = this.workflowFactory.taskRepository();
    var workflowService = new WorkflowServiceImpl(this.workflowRegistry, publisher, domainChannel[0], definitionRepository,
        instanceRepository, taskRepository);

    for (var definition : this.workflowFactory.getDefinitions()) {
      workflowService.deployDefinition(definition);
    }

    // Start service
    var server = this.apiFactory.addService(workflowService).server();
    server.start();

    this.serviceRegistration.register("process");
    this.serviceRegistration.register("task");

    // This resources will be used by workflow delegates.
    DelegateUtils.setDataClient(dataClient);
    DelegateUtils.setPublisher(publisher);
    DelegateUtils.setChannels(channels);
    DelegateUtils.setConfiguration(this.configuration);
  }

  private String getWorkflowDomain(String configTemplate) throws ConfigException {
    var configName = String.format(configTemplate, this.serviceName);
    return this.configuration.getConfig(configName)
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
