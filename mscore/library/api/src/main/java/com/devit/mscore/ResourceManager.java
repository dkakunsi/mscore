package com.devit.mscore;

import static com.devit.mscore.util.AttributeConstants.getName;

import java.io.File;
import java.nio.file.Paths;

import com.devit.mscore.exception.ConfigException;
import com.devit.mscore.exception.RegistryException;
import com.devit.mscore.exception.ResourceException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Interface to manage system resource.
 * 
 * @author dkakunsi
 */
public abstract class ResourceManager extends Manager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceManager.class);

    protected Registry registry;

    protected String resourceType;

    protected ResourceManager(String resourceType, Configuration configuration, Registry registry) {
        super(configuration);
        this.registry = registry;
        this.resourceType = resourceType;
    }

    /**
     * Register the resource.
     * 
     * @param context of the request.
     * @throws RegistryException
     * @throws ResourceException
     */
    public void registerResources(ApplicationContext context) throws ResourceException {
        try {
            var location = getResourceLocation(context);
            if (StringUtils.isBlank(location)) {
                LOGGER.info("BreadcrumbdId: {}. No resource config found", context.getBreadcrumbId());
                return;
            }

            var directory = Paths.get(location).toFile();
            if (!directory.exists()) {
                LOGGER.info("BreadcrumbdId: {}. No resource found in '{}'", context.getBreadcrumbId(), location);
                return;
            }

            for (var resourceFile : Resource.getFiles(directory)) {
                registerResource(context, resourceFile);
            }
        } catch (RegistryException | ConfigException ex) {
            throw new ResourceException("Cannot register resource.", ex);
        }
    }

    private void registerResource(ApplicationContext context, File resourceFile) throws ResourceException, RegistryException {
        var resource = createResource(resourceFile);
        LOGGER.debug("BreadcrumbId: {}. Registering resource: {}", context.getBreadcrumbId(), resource);
        var message = resource.getMessage();
        this.registry.add(context, getName(message), message.toString());
        LOGGER.info("BreadcrumbId: {}. Resource {} is added to registry.", context.getBreadcrumbId(), resource.getName());
    }

    public String getType() {
        return this.resourceType;
    }

    protected abstract String getResourceLocation(ApplicationContext context) throws ConfigException;

    protected abstract Resource createResource(File file) throws ResourceException;
}
