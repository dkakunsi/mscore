package com.devit.mscore.filter;

import java.util.List;

import com.devit.mscore.ApplicationContext;
import com.devit.mscore.Configuration;
import com.devit.mscore.exception.ConfigException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FilterFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(FilterFactory.class);

    private static final String REMOVING = "services.%s.filter.remove";
    
    public static FilterFactory of() {
        return new FilterFactory();
    }

    public FiltersExecutor filters(ApplicationContext context, Configuration configuration) {
        var executors = new FiltersExecutor();
        addRemovingFilter(context, configuration, executors);
        return executors;
    }

    private void addRemovingFilter(ApplicationContext context, Configuration configuration, FiltersExecutor executors) {
        var configName = String.format(REMOVING, configuration.getServiceName());
        try {
            var removingAttributes = configuration.getConfig(context, configName);
            removingAttributes.ifPresent(removingAttribute -> {
                var attributes = List.of(removingAttribute.split(","));
                executors.add(new RemovingFilter(attributes));
            });
        } catch (ConfigException ex) {
            LOGGER.warn("BreadcrumbId: {}. Cannot add removing filter.", context.getBreadcrumbId(), ex);
        }
    }
}
