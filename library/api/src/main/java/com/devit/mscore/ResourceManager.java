package com.devit.mscore;

import static com.devit.mscore.util.AttributeConstants.getName;

import com.devit.mscore.exception.ConfigException;
import com.devit.mscore.exception.RegistryException;
import com.devit.mscore.exception.ResourceException;

import java.io.File;
import java.nio.file.Paths;

import org.apache.commons.lang3.StringUtils;

/**
 * Interface to manage system resource.
 *
 * @author dkakunsi
 */
public abstract class ResourceManager extends Manager {

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
   * @throws RegistryException
   * @throws ResourceException
   */
  public void registerResources() throws ResourceException {
    try {
      var location = getResourceLocation();
      if (StringUtils.isBlank(location)) {
        return;
      }

      var directory = Paths.get(location).toFile();
      if (!directory.exists()) {
        return;
      }

      for (var resourceFile : Resource.getFiles(directory)) {
        registerResource(resourceFile);
      }
    } catch (RegistryException | ConfigException ex) {
      throw new ResourceException("Cannot register resource", ex);
    }
  }

  private void registerResource(File resourceFile) throws ResourceException, RegistryException {
    var resource = createResource(resourceFile);
    var message = resource.getMessage();
    this.registry.add(getName(message), message.toString());
  }

  public String getType() {
    return this.resourceType;
  }

  protected abstract String getResourceLocation() throws ConfigException;

  protected abstract Resource createResource(File file) throws ResourceException;
}
