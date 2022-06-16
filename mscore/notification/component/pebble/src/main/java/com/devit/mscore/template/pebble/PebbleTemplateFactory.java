package com.devit.mscore.template.pebble;

import java.io.File;

import com.devit.mscore.ApplicationContext;
import com.devit.mscore.Configuration;
import com.devit.mscore.Registry;
import com.devit.mscore.Resource;
import com.devit.mscore.ResourceManager;
import com.devit.mscore.exception.ConfigException;
import com.devit.mscore.exception.ResourceException;

public class PebbleTemplateFactory extends ResourceManager {

    private static final String LOCATION = "template.resource.location";

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
    protected String getResourceLocation(ApplicationContext context) {
        try {
            return this.configuration.getConfig(context, LOCATION).orElse(null);
        } catch (ConfigException ex) {
            return null;
        }
    }

    @Override
    protected Resource createResource(File file) throws ResourceException {
        return new PebbleTemplateResource(file);
    }
}
