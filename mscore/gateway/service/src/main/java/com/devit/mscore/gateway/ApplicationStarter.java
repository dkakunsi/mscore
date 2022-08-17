package com.devit.mscore.gateway;

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
import com.devit.mscore.gateway.service.ResourceService;
import com.devit.mscore.gateway.service.WorkflowService;
import com.devit.mscore.registry.ZookeeperRegistryFactory;
import com.devit.mscore.util.DateUtils;
import com.devit.mscore.web.Client;

import org.apache.commons.lang3.BooleanUtils;

public class ApplicationStarter implements Starter {

  private static final String WORKFLOW_SERVICES = "services.%s.workflow.enabled";

  private static final String TIMEZONE = "platform.service.timezone";

  private Configuration configuration;

  private ServiceRegistration serviceRegistration;

  private AuthenticationProvider authenticationProvider;

  private Client webClient;

  private ApiFactory apiFactory;

  public ApplicationStarter(String[] args) throws ConfigException {
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
  }

  @Override
  public void start() throws ApplicationException {
    var useWorkflow = isUseWorkflow();
    var workflowService = new WorkflowService(this.serviceRegistration, this.webClient);
    var resourceService = new ResourceService(this.serviceRegistration, this.webClient, workflowService, useWorkflow);

    this.apiFactory.add(resourceService);

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
    System.exit(0);
  }
}
