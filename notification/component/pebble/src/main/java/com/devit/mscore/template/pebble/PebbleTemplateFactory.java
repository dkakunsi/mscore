package com.devit.mscore.template.pebble;

import com.devit.mscore.Configuration;
import com.devit.mscore.Registry;
import com.devit.mscore.Resource;
import com.devit.mscore.ResourceManager;
import com.devit.mscore.exception.ConfigException;
import com.devit.mscore.exception.ResourceException;

import java.io.File;

public class PebbleTemplateFactory extends ResourceManager {

  private static final String LOCATION = "services.%s.template.resource.location";

  private PebbleTemplateFactory(Registry registry, Configuration configuration) {
    super("template", configuration, registry);
  }

  public static PebbleTemplateFactory of(Registry registry, Configuration configuration) {
    return new PebbleTemplateFactory(registry, configuration);
  }

  public PebbleTemplate template() {
    return new PebbleTemplate();
  }

  @Override
  protected String getResourceLocation() {
    var configName = String.format(LOCATION, configuration.getServiceName());
    try {
      return configuration.getConfig(configName).orElse(null);
    } catch (ConfigException ex) {
      return null;
    }
  }

  @Override
  protected Resource createResource(File file) throws ResourceException {
    return new PebbleTemplateResource(file);
  }
}
