package com.devit.mscore.data;

import static com.devit.mscore.util.Utils.INDEX_MAP;
import static com.devit.mscore.util.Utils.SCHEMA;

import com.devit.mscore.Configuration;
import com.devit.mscore.Index;
import com.devit.mscore.Logger;
import com.devit.mscore.Registry;
import com.devit.mscore.ResourceManager;
import com.devit.mscore.Schema;
import com.devit.mscore.ServiceRegistration;
import com.devit.mscore.Starter;
import com.devit.mscore.Validation;
import com.devit.mscore.authentication.JWTAuthenticationProvider;
import com.devit.mscore.configuration.FileConfiguration;
import com.devit.mscore.configuration.FileConfigurationUtils;
import com.devit.mscore.configuration.ZookeeperConfiguration;
import com.devit.mscore.data.enrichment.EnrichmentsExecutor;
import com.devit.mscore.data.enrichment.IndexEnrichment;
import com.devit.mscore.data.filter.FilterFactory;
import com.devit.mscore.data.observer.IndexingObserver;
import com.devit.mscore.data.observer.PublishingObserver;
import com.devit.mscore.data.service.DefaultService;
import com.devit.mscore.data.synchronization.SynchronizationListener;
import com.devit.mscore.data.synchronization.SynchronizationsExecutor;
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

  private static final String DEPENDENCY = "services.%s.kafka.topic.dependency";

  private static final String TIMEZONE = "platform.service.timezone";

  private static final String PUBLISHING_DELAY = "platform.kafka.publishing.delay";

  private static final String DEFAULT_ZONE_ID = "Asia/Makassar";

  private Configuration configuration;

  private Registry schemaRegistry;

  private Registry indexMapRegistry;

  private Registry serviceRegistry;

  private List<Validation> webValidations;

  private Map<String, Index> indices;

  private ValidationsExecutor validationsExecutor;

  private EnrichmentsExecutor enrichmentsExecutor;

  private SynchronizationsExecutor synchronizationsExecutor;

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
    this.synchronizationsExecutor = new SynchronizationsExecutor();

    this.webValidations = new ArrayList<>();
    this.indices = new HashMap<>();
  }

  public static ApplicationStarter of(String... args) throws ConfigException {
    return new ApplicationStarter(args);
  }

  @Override
  public void start() throws ApplicationException {
    // Register resources
    var schemaManager = SchemaManager.of(this.configuration, this.schemaRegistry);
    registerResource(schemaManager);

    var indexFactory = ElasticsearchIndexFactory.of(this.configuration, this.indexMapRegistry);
    registerResource(indexFactory);

    // Create authentication
    var authentication = JWTAuthenticationProvider.of(this.configuration);

    this.webValidations.add(new SchemaValidation(this.schemaRegistry));

    // Create component factories
    var repositoryFactory = MongoDatabaseFactory.of(this.configuration);
    var messagingFactory = KafkaMessagingFactory.of(this.configuration);
    var apiFactory = JavalinApiFactory.of(this.configuration, authentication, this.webValidations);
    var filterFactory = FilterFactory.of();

    var filter = filterFactory.filters(this.configuration);
    var registration = new ServiceRegistration(this.serviceRegistry, this.configuration);

    // Generate service and it's dependencies from schema.
    var delay = getPublishingDelay();
    for (var schema : schemaManager.getSchemas()) {
      var index = indexFactory.index(schema.getDomain());
      this.indices.put(schema.getDomain(), index.build());

      createEnrichment(schema, this.enrichmentsExecutor, this.indices);

      var repository = repositoryFactory.repository(schema);
      var publisher = messagingFactory.publisher(schema.getDomain());
      var indexingObserver = new IndexingObserver(index);
      var publishingObserver = new PublishingObserver(publisher, delay);
      var service = new DefaultService(schema, repository, index, this.validationsExecutor, filter,
          this.enrichmentsExecutor)
          .addObserver(indexingObserver)
          .addObserver(publishingObserver);

      apiFactory.add(service);
      registration.register(service);

      this.synchronizationsExecutor.add(service);
    }

    for (var schema : schemaManager.getSchemas()) {
      createEnrichment(schema, this.enrichmentsExecutor, this.indices);
    }

    // Create listener
    var syncTopics = messagingFactory.getTopics(String.format(DEPENDENCY, configuration.getServiceName()));
    if (syncTopics.isPresent()) {
      var subscriber = messagingFactory.subscriber();
      var syncListener = new SynchronizationListener(subscriber, this.synchronizationsExecutor);
      syncListener.listen(syncTopics.get());
    }

    // Start API server
    apiFactory.server().start();

    this.serviceRegistry.close();
  }

  private Long getPublishingDelay() throws ConfigException {
    var wait = this.configuration.getConfig(PUBLISHING_DELAY).orElse("0");
    try {
      return Long.parseLong(wait);
    } catch (NumberFormatException ex) {
      return 0L;
    }
  }

  private static void createEnrichment(Schema schema, EnrichmentsExecutor enricher, Map<String, Index> indices) {
    for (var attribute : schema.getReferenceNames()) {
      enricher.add(new IndexEnrichment(indices, schema.getDomain(), attribute));
    }
  }

  @SuppressWarnings("PMD.GuardLogStatement")
  private static void registerResource(ResourceManager resourceManager) {
    LOGGER.info("Register resource: {}.", resourceManager.getType());
    try {
      resourceManager.registerResources();
    } catch (ResourceException ex) {
      LOGGER.warn("Cannot register resource {}.", resourceManager.getType(), ex);
    }
  }

  @Override
  public void stop() {
    throw new RuntimeException("Application is stopped");
  }
}
