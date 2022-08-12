package com.devit.mscore.gateway;

import static com.devit.mscore.ApplicationContext.setContext;

import com.devit.mscore.DefaultApplicationContext;
import com.devit.mscore.Logger;
import com.devit.mscore.ServiceRegistration;
import com.devit.mscore.authentication.JWTAuthenticationProvider;
import com.devit.mscore.configuration.FileConfigurationUtils;
import com.devit.mscore.configuration.ZookeeperConfiguration;
import com.devit.mscore.exception.ApplicationException;
import com.devit.mscore.gateway.api.ApiFactory;
import com.devit.mscore.gateway.service.ResourceService;
import com.devit.mscore.gateway.service.WorkflowService;
import com.devit.mscore.logging.ApplicationLogger;
import com.devit.mscore.registry.ZookeeperRegistryFactory;
import com.devit.mscore.util.DateUtils;
import com.devit.mscore.web.jersey.JerseyClientFactory;

import org.apache.commons.lang3.BooleanUtils;

public class Main {

    private static final Logger LOGGER = ApplicationLogger.getLogger(Main.class);

    private static final String WORKFLOW_SERVICES = "services.%s.workflow.enabled";

    private static final String TIMEZONE = "platform.service.timezone";

    public static void main(String[] args) throws ApplicationException {
        setContext(DefaultApplicationContext.of("starter"));
        try {
            LOGGER.info("Service is starting...");
            start(args);
            LOGGER.info("Service is started!");
        } catch (RuntimeException ex) {
            throw new ApplicationException("Service is fail to start.", ex);
        }
    }

    // TODO: use application starter
    private static void start(String[] args) throws ApplicationException {
        var fileConfiguration = FileConfigurationUtils.load(args);
        var serviceName = fileConfiguration.getServiceName();

        var zookeeperRegistry = ZookeeperRegistryFactory.of(fileConfiguration).registry("platformConfig");
        zookeeperRegistry.open();
        var configuration = new ZookeeperConfiguration(zookeeperRegistry, serviceName);

        DateUtils.setZoneId(configuration.getConfig(TIMEZONE).orElse("Asia/Makassar"));

        // Init registry
        var serviceRegistry = zookeeperRegistry;
        var serviceRegistration = new ServiceRegistration(serviceRegistry, configuration); 

        // Create authentication
        var authentication = JWTAuthenticationProvider.of(configuration);
        var configName = String.format(WORKFLOW_SERVICES, serviceName);
        var useWorkflow = Boolean.valueOf(configuration.getConfig(configName).orElse("false"));

        // Init service
        var client = JerseyClientFactory.of().client();
        var workflowService = new WorkflowService(serviceRegistration, client);
        var resourceService = new ResourceService(serviceRegistration, client, workflowService, useWorkflow);

        // Start service
        var apiFactory = ApiFactory.of(configuration, authentication);
        apiFactory.add(resourceService);

        if (BooleanUtils.isTrue(useWorkflow)) {
            apiFactory.add(workflowService);
        }

        var server = apiFactory.server();
        server.start();

        serviceRegistration.open();
        serviceRegistration.register(resourceService);
        serviceRegistration.register(workflowService);
    }
}
