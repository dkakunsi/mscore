package com.devit.mscore;

import static com.devit.mscore.util.Utils.INDEX_MAP;
import static com.devit.mscore.util.Utils.SCHEMA;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.devit.mscore.authentication.JWTAuthenticationProvider;
import com.devit.mscore.configuration.FileConfiguration;
import com.devit.mscore.configuration.FileConfigurationUtils;
import com.devit.mscore.configuration.ZookeeperConfiguration;
import com.devit.mscore.db.mongo.MongoDatabaseFactory;
import com.devit.mscore.enrichment.EnrichmentsExecutor;
import com.devit.mscore.enrichment.IndexEnrichment;
import com.devit.mscore.exception.ApplicationException;
import com.devit.mscore.exception.ConfigException;
import com.devit.mscore.exception.RegistryException;
import com.devit.mscore.exception.ResourceException;
import com.devit.mscore.filter.FilterFactory;
import com.devit.mscore.indexing.elasticsearch.ElasticsearchIndexFactory;
import com.devit.mscore.messaging.kafka.KafkaMessagingFactory;
import com.devit.mscore.observer.IndexingObserver;
import com.devit.mscore.observer.PublishingObserver;
import com.devit.mscore.registry.MemoryRegistry;
import com.devit.mscore.registry.ZookeeperRegistryFactory;
import com.devit.mscore.schema.everit.SchemaManager;
import com.devit.mscore.schema.everit.SchemaValidation;
import com.devit.mscore.service.DefaultService;
import com.devit.mscore.synchronization.SynchronizationListener;
import com.devit.mscore.synchronization.SynchronizationsExecutor;
import com.devit.mscore.util.DateUtils;
import com.devit.mscore.validation.ValidationsExecutor;
import com.devit.mscore.web.javalin.JavalinApiFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApplicationStarter implements Starter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationStarter.class);

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

    private ApplicationContext context;

    public ApplicationStarter(String[] args) throws ConfigException {
        this(FileConfigurationUtils.load(args));
    }

    public ApplicationStarter(FileConfiguration fileConfiguration) throws ConfigException {
        this.context = DefaultApplicationContext.of("starter");

        try {
            var registryFactory = ZookeeperRegistryFactory.of(fileConfiguration);
            var zookeeperRegistry = registryFactory.registry(this.context, "platformConfig");
            zookeeperRegistry.open();
            this.configuration = new ZookeeperConfiguration(this.context, zookeeperRegistry, fileConfiguration.getServiceName());
            this.serviceRegistry = zookeeperRegistry;
        } catch (RegistryException ex) {
            throw new ConfigException(ex);
        }
        DateUtils.setZoneId(this.configuration.getConfig(this.context, TIMEZONE).orElse(DEFAULT_ZONE_ID));

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

    public static ApplicationStarter of(String[] args) throws ConfigException {
        var starter = new ApplicationStarter(args);
        var schemaValidation = new SchemaValidation(starter.getSchemaRegistry());
        starter.addWebValidations(schemaValidation);

        return starter;
    }

    public void addWebValidations(Validation validation) {
        this.webValidations.add(validation);
    }

    public Registry getSchemaRegistry() {
        return this.schemaRegistry;
    }

    @Override
    public void start() throws ApplicationException {
        // Register resources
        var schemaManager = SchemaManager.of(this.configuration, this.schemaRegistry);
        registerResource(schemaManager, this.context);

        var indexFactory = ElasticsearchIndexFactory.of(this.configuration, this.indexMapRegistry);
        registerResource(indexFactory, this.context);

        // Create authentication
        var authentication = JWTAuthenticationProvider.of(this.context, this.configuration);

        // Create component factories
        var repositoryFactory = MongoDatabaseFactory.of(this.configuration);
        var messagingFactory = KafkaMessagingFactory.of(this.context, this.configuration);
        var apiFactory = JavalinApiFactory.of(this.context, this.configuration, authentication, this.webValidations);
        var filterFactory = FilterFactory.of();

        var filter = filterFactory.filters(this.context, this.configuration);
        var registration = new ServiceRegistration(this.serviceRegistry, this.configuration);

        // Generate service and it's dependencies from schema.
        var delay = getPublishingDelay();
        for (var schema : schemaManager.getSchemas()) {
            var index = indexFactory.index(this.context, schema.getDomain());
            this.indices.put(schema.getDomain(), index.build(this.context));

            createEnrichment(schema, this.enrichmentsExecutor, this.indices);

            var repository = repositoryFactory.repository(this.context, schema);
            var publisher = messagingFactory.publisher(schema.getDomain());
            var indexingObserver = new IndexingObserver(index);
            var publishingObserver = new PublishingObserver(publisher, delay);
            var service = new DefaultService(schema, repository, index, this.validationsExecutor, filter, this.enrichmentsExecutor)
                    .addObserver(indexingObserver)
                    .addObserver(publishingObserver);

            apiFactory.add(service);
            registration.register(this.context, service);

            this.synchronizationsExecutor.add(service);
        }

        // Create listener
        var syncTopics = messagingFactory.getTopics(this.context, String.format(DEPENDENCY, configuration.getServiceName()));
        if (syncTopics.isPresent()) {
            var subscriber = messagingFactory.subscriber();
            var syncListener = new SynchronizationListener(subscriber, this.synchronizationsExecutor);
            syncListener.listen(syncTopics.get());
        }

        // Start API server
        apiFactory.server(this.context).start();

        this.serviceRegistry.close();
    }

    private Long getPublishingDelay() throws ConfigException {
        var wait = this.configuration.getConfig(this.context, PUBLISHING_DELAY).orElse("0");
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

    private static void registerResource(ResourceManager resourceManager, ApplicationContext context) {
        LOGGER.info("BreadcrumbId: {}. Register resource: {}.", context.getBreadcrumbId(), resourceManager.getType());
        try {
            resourceManager.registerResources(context);
        } catch (ResourceException ex) {
            LOGGER.warn("BreadcrumbId: {}. Cannot register resource {}.", context.getBreadcrumbId(),
                    resourceManager.getType(), ex);
        }
    }

    @Override
    public void stop() {
        System.exit(0);
    }
}
