package com.devit.mscore.workflow;

import static com.devit.mscore.util.Utils.WORKFLOW;
import static com.devit.mscore.workflow.flowable.DelegateUtils.NOTIFICATION;

import java.util.HashMap;

import com.devit.mscore.ApplicationContext;
import com.devit.mscore.Configuration;
import com.devit.mscore.DefaultApplicationContext;
import com.devit.mscore.Publisher;
import com.devit.mscore.ResourceManager;
import com.devit.mscore.Service;
import com.devit.mscore.ServiceRegistration;
import com.devit.mscore.authentication.JWTAuthenticationProvider;
import com.devit.mscore.configuration.FileConfigurationUtils;
import com.devit.mscore.configuration.ZookeeperConfiguration;
import com.devit.mscore.exception.ApplicationException;
import com.devit.mscore.exception.ConfigException;
import com.devit.mscore.exception.ResourceException;
import com.devit.mscore.messaging.kafka.KafkaMessagingFactory;
import com.devit.mscore.registry.MemoryRegistry;
import com.devit.mscore.registry.ZookeeperRegistryFactory;
import com.devit.mscore.util.DateUtils;
import com.devit.mscore.web.jersey.JerseyClientFactory;
import com.devit.mscore.workflow.api.ApiFactory;
import com.devit.mscore.workflow.flowable.DataClient;
import com.devit.mscore.workflow.flowable.DelegateUtils;
import com.devit.mscore.workflow.flowable.FlowableWorkflowFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    private static final String WORKFLOW_DOMAIN = "services.%s.domain.workflow";

    private static final String TASK_DOMAIN = "services.%s.domain.task";

    private static final String TIMEZONE = "platform.service.timezone";

    public static void main(String[] args) throws ApplicationException {
        try {
            LOGGER.info("Service is starting...");
            start(args);
            LOGGER.info("Service is started!");
        } catch (RuntimeException ex) {
            throw new ApplicationException("Service is fail to start.", ex);
        }
    }

    private static void start(String[] args) throws ApplicationException {
        var fileConfiguration = FileConfigurationUtils.load(args);
        var serviceName = fileConfiguration.getServiceName();
        var context = DefaultApplicationContext.of("starter");

        var zookeeperRegistry = ZookeeperRegistryFactory.of(fileConfiguration).registry(context, "platformCOnfig");
        zookeeperRegistry.open();
        var configuration = new ZookeeperConfiguration(context, zookeeperRegistry, serviceName);

        DateUtils.setZoneId(configuration.getConfig(context, TIMEZONE).orElse("Asia/Makassar"));

        var workflowRegistry = new MemoryRegistry(WORKFLOW);
        var workflowFactory = FlowableWorkflowFactory.of(configuration, workflowRegistry);
        registerResource(workflowFactory, context);

        var messagingFactory = KafkaMessagingFactory.of(context, configuration);

        // Create publisher
        var publishers = new HashMap<String, Publisher>();
        publishers.put(NOTIFICATION, messagingFactory.publisher(NOTIFICATION));

        // Create authentication
        var authentication = JWTAuthenticationProvider.of(context, configuration);

        // Create data client object
        var serviceRegistry = zookeeperRegistry;
        var serviceRegistration = new ServiceRegistration(serviceRegistry, configuration);

        var client = JerseyClientFactory.of().client();
        var workflowDomain = getWorkflowInstanceDomainName(context, configuration, serviceName);
        var taskDomain = getWorkflowTaskDomainName(context, configuration, serviceName);
        var dataClient = new DataClient(client, serviceRegistration, workflowDomain, taskDomain);

        // Init service
        var apiFactory = ApiFactory.of(context, configuration, authentication);

        var workflowProcess = workflowFactory.workflowProcess(context, workflowRegistry, dataClient);
        workflowProcess.start();

        for (var definition : workflowFactory.getDefinitions(context)) {
            workflowProcess.deployDefinition(context, definition);
        }

        // Start service
        var server = apiFactory.addService((Service) workflowProcess).server(context);
        server.start();

        serviceRegistration.register(context, (Service) workflowProcess);

        // This resources will be used by workflow delegates.
        DelegateUtils.setDataClient(dataClient);
        DelegateUtils.setPublishers(publishers);
        DelegateUtils.setConfiguration(configuration);

        LOGGER.info("BreadcrumbId: {}. Service is started.", context.getBreadcrumbId());
    }

    private static String getWorkflowInstanceDomainName(ApplicationContext context, Configuration configuration, String serviceName) throws ConfigException {
        var configName = String.format(WORKFLOW_DOMAIN, serviceName);
        return configuration.getConfig(context, configName).orElseThrow(() -> new ConfigException("Workflow domain name is not configured"));
    }

    private static String getWorkflowTaskDomainName(ApplicationContext context, Configuration configuration, String serviceName) throws ConfigException {
        var configName = String.format(TASK_DOMAIN, serviceName);
        return configuration.getConfig(context, configName).orElseThrow(() -> new ConfigException("Workflow task domain name is not configured"));
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

}
