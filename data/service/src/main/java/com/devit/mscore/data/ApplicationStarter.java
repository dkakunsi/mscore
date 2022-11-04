package com.devit.mscore.data;

import static com.devit.mscore.util.Utils.INDEX_MAP;
import static com.devit.mscore.util.Utils.SCHEMA;

import com.devit.mscore.Configuration;
import com.devit.mscore.FiltersExecutor;
import com.devit.mscore.Index;
import com.devit.mscore.Logger;
import com.devit.mscore.Publisher;
import com.devit.mscore.Registry;
import com.devit.mscore.ResourceManager;
import com.devit.mscore.Schema;
import com.devit.mscore.Service;
import com.devit.mscore.ServiceRegistration;
import com.devit.mscore.Starter;
import com.devit.mscore.Validation;
import com.devit.mscore.authentication.JWTAuthenticationProvider;
import com.devit.mscore.configuration.FileConfiguration;
import com.devit.mscore.configuration.FileConfigurationUtils;
import com.devit.mscore.configuration.ZookeeperConfiguration;
import com.devit.mscore.data.enrichment.EnrichmentsExecutor;
import com.devit.mscore.data.enrichment.IndexEnrichment;
import com.devit.mscore.data.filter.RemovingFilter;
import com.devit.mscore.data.observer.IndexingObserver;
import com.devit.mscore.data.observer.PublishingObserver;
import com.devit.mscore.data.observer.SynchronizationObserver;
import com.devit.mscore.data.service.DefaultService;
import com.devit.mscore.data.synchronization.IndexSynchronization;
import com.devit.mscore.data.validation.ValidationsExecutor;
import com.devit.mscore.db.mongo.MongoDatabaseFactory;
import com.devit.mscore.exception.ApplicationException;
import com.devit.mscore.exception.ConfigException;
import com.devit.mscore.exception.RegistryException;
import com.devit.mscore.exception.ResourceException;
import com.devit.mscore.indexing.elasticsearch.ElasticsearchIndexFactory;
import com.devit.mscore.logging.ApplicationLogger;
import com.devit.mscore.messaging.kafka.KafkaMessagingFactory;
import com.devit.mscore.registry.MemoryRegistry;
import com.devit.mscore.registry.ZookeeperRegistryFactory;
import com.devit.mscore.schema.everit.SchemaManager;
import com.devit.mscore.schema.everit.SchemaValidation;
import com.devit.mscore.util.DateUtils;
import com.devit.mscore.web.javalin.JavalinApiFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApplicationStarter implements Starter {

  private static final Logger LOGGER = ApplicationLogger.getLogger(ApplicationStarter.class);

  private static final String EVENT_TOPIC = "services.%s.listen.topics";

  private static final String TIMEZONE = "platform.service.timezone";

  private static final String DEFAULT_ZONE_ID = "Asia/Makassar";

  private static final String REMOVING = "services.%s.filter.remove";

  private Configuration configuration;

  private Registry schemaRegistry;

  private Registry indexMapRegistry;

  private Registry serviceRegistry;

  private List<Validation> webValidations;

  private Map<String, Index> indices;

  private ValidationsExecutor validationsExecutor;

  private EnrichmentsExecutor enrichmentsExecutor;

  private MongoDatabaseFactory repositoryFactory;

  private KafkaMessagingFactory messagingFactory;

  private SchemaManager schemaManager;

  private ElasticsearchIndexFactory indexFactory;

  private JWTAuthenticationProvider authentication;

  private JavalinApiFactory apiFactory;

  public ApplicationStarter(String... args) throws ConfigException {
    this(FileConfigurationUtils.load(args));
  }

  public ApplicationStarter(FileConfiguration fileConfiguration) throws ConfigException {
    try {
      var registryFactory = ZookeeperRegistryFactory.of(fileConfiguration);
      var zookeeperRegistry = registryFactory.registry("platformConfig");
      zookeeperRegistry.open();
      this.configuration = new ZookeeperConfiguration(zookeeperRegistry, fileConfiguration.getServiceName());
      this.serviceRegistry = zookeeperRegistry;
    } catch (RegistryException ex) {
      throw new ConfigException(ex);
    }
    DateUtils.setZoneId(this.configuration.getConfig(TIMEZONE).orElse(DEFAULT_ZONE_ID));

    // Create registry
    this.schemaRegistry = new MemoryRegistry(SCHEMA);
    this.indexMapRegistry = new MemoryRegistry(INDEX_MAP);

    // Create executor
    this.validationsExecutor = new ValidationsExecutor();
    this.enrichmentsExecutor = new EnrichmentsExecutor();

    this.webValidations = new ArrayList<>();
    this.indices = new HashMap<>();

    repositoryFactory = MongoDatabaseFactory.of(this.configuration);
    messagingFactory = KafkaMessagingFactory.of(this.configuration);
    schemaManager = SchemaManager.of(this.configuration, this.schemaRegistry);
    indexFactory = ElasticsearchIndexFactory.of(this.configuration, this.indexMapRegistry);
    authentication = JWTAuthenticationProvider.of(this.configuration);
    apiFactory = JavalinApiFactory.of(this.configuration, authentication, this.webValidations);
  }

  public static ApplicationStarter of(String... args) throws ConfigException {
    return new ApplicationStarter(args);
  }

  @Override
  public void start() throws ApplicationException {
    // Register resources
    registerResource(schemaManager);
    registerResource(indexFactory);

    webValidations.add(new SchemaValidation(this.schemaRegistry));

    var filterExecutor = new FiltersExecutor();
    createFilter(configuration, filterExecutor);

    var registration = new ServiceRegistration(this.serviceRegistry, this.configuration);
    var syncObserver = new SynchronizationObserver();
    var publisher = messagingFactory.publisher();
    var services = new HashMap<String, Service>();

    // Generate service and it's dependencies from schema.
    for (var s : schemaManager.getSchemas()) {
      var service = processSchema(s, filterExecutor, syncObserver, publisher);
      // set synchronizer to cotroller.
      apiFactory.add(service);
      registration.register(service);
      services.put(service.getDomain(), service);
    }

    createListener(messagingFactory, services);
    apiFactory.server().start();
    this.serviceRegistry.close();
  }

  private Service processSchema(Schema schema, FiltersExecutor filters, SynchronizationObserver so, Publisher publisher)
      throws RegistryException, ConfigException {

    var index = indexFactory.index(schema.getDomain());
    var indexingObserver = new IndexingObserver(index, enrichmentsExecutor, so);
    indices.put(schema.getDomain(), index.build());

    schema.getReferences().forEach((attr, refDomains) -> {
      enrichmentsExecutor.add(new IndexEnrichment(indices, schema.getDomain(), attr));
      refDomains.forEach(rd -> so.add(new IndexSynchronization(index, rd, attr).with(filters).with(indexingObserver)));
    });

    var repository = repositoryFactory.repository(schema);
    var service = new DefaultService(schema, repository, index, validationsExecutor, filters)
        .addObserver(indexingObserver);

    var completedEvent = String.format("%s.completed", schema.getDomain());
    var eventChannel = messagingFactory.getTopic(completedEvent);
    eventChannel.ifPresent(e -> service.addObserver(new PublishingObserver(publisher, e)));

    return service;
  }

  private void createListener(KafkaMessagingFactory messagingFactory, Map<String, Service> services)
      throws ApplicationException {
    var topicConfig = String.format(EVENT_TOPIC, configuration.getServiceName());
    var eventTopic = messagingFactory.getTopics(topicConfig);
    if (eventTopic.isPresent()) {
      LOGGER.info("Listening to topics '{}'", eventTopic);
      var subscriber = messagingFactory.subscriber();
      var eventListener = EventListener.of(subscriber, services);
      eventListener.listen(eventTopic.get());
    }
  }

  private static void registerResource(ResourceManager resourceManager) {
    LOGGER.info("Registering resource '{}'", resourceManager.getType());
    try {
      resourceManager.registerResources();
    } catch (ResourceException ex) {
      LOGGER.warn("Cannot register resource '{}'", resourceManager.getType(), ex);
    }
  }


  private void createFilter(Configuration configuration, FiltersExecutor executors) {
    var configName = String.format(REMOVING, configuration.getServiceName());
    try {
      var removingAttributes = configuration.getConfig(configName);
      removingAttributes.ifPresent(removingAttribute -> {
        var attributes = List.of(removingAttribute.split(","));
        executors.add(new RemovingFilter(attributes));
      });
    } catch (ConfigException ex) {
      LOGGER.warn("Cannot add removing filter", ex);
    }
  }

  @Override
  public void stop() {
    throw new RuntimeException("Application is stopped");
  }
}
