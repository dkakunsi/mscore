package com.devit.mscore.data.filter;

import com.devit.mscore.Configuration;
import com.devit.mscore.Logger;
import com.devit.mscore.exception.ConfigException;
import com.devit.mscore.logging.ApplicationLogger;

import java.util.List;

public class FilterFactory {

  private static final Logger LOGGER = new ApplicationLogger(FilterFactory.class);

  private static final String REMOVING = "services.%s.filter.remove";

  public static FilterFactory of() {
    return new FilterFactory();
  }

  public FiltersExecutor filters(Configuration configuration) {
    var executors = new FiltersExecutor();
    addRemovingFilter(configuration, executors);
    return executors;
  }

  private void addRemovingFilter(Configuration configuration, FiltersExecutor executors) {
    var configName = String.format(REMOVING, configuration.getServiceName());
    try {
      var removingAttributes = configuration.getConfig(configName);
      removingAttributes.ifPresent(removingAttribute -> {
        var attributes = List.of(removingAttribute.split(","));
        executors.add(new RemovingFilter(attributes));
      });
    } catch (ConfigException ex) {
      LOGGER.warn("Cannot add removing filter.", ex);
    }
  }
}
