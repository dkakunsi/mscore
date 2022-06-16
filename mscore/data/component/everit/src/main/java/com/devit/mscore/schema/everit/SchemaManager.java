package com.devit.mscore.schema.everit;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.devit.mscore.ApplicationContext;
import com.devit.mscore.Configuration;
import com.devit.mscore.Registry;
import com.devit.mscore.Resource;
import com.devit.mscore.ResourceManager;
import com.devit.mscore.Schema;
import com.devit.mscore.exception.ConfigException;
import com.devit.mscore.exception.RegistryException;
import com.devit.mscore.exception.ResourceException;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SchemaManager extends ResourceManager {

    private static final Logger LOG = LoggerFactory.getLogger(SchemaManager.class);

    private static final String LOCATION = "services.%s.schema.resource.location";

    private List<Schema> schemas;

    private SchemaManager(Configuration configuration, Registry registry) {
        super("schema", configuration, registry);
        this.schemas = new ArrayList<>();
    }

    public List<Schema> getSchemas() {
        return this.schemas;
    }

    @Override
    protected String getResourceLocation(ApplicationContext context) {
        var configName = String.format(LOCATION, this.configuration.getServiceName());
        try {
            return this.configuration.getConfig(context, configName).orElse(null);
        } catch (ConfigException ex) {
            return null;
        }
    }

    @Override
    protected Resource createResource(File file) throws ResourceException {
        var schema = new JSONSchema(file);
        this.schemas.add(schema);
        return schema;
    }

    /**
     * Load schema from registry.
     * 
     * @param context of the request.
     * @param domain  name.
     * @return domain schema.
     * @throws JSONException     cannot parse JSON content.
     * @throws RegistryException cannotconnect to registry.
     */
    public JSONSchema getSchema(ApplicationContext context, String domain) throws JSONException, RegistryException {
        LOG.info("BreadcrumbId: {}. Loading schema: {}", context.getBreadcrumbId(), domain);
        var schema = this.registry.get(context, domain);
        LOG.info("BreadcrumbId: {}. Retrieved schema: {}", context.getBreadcrumbId(), schema);
        return new JSONSchema(new JSONObject(schema));
    }

    public static SchemaManager of(Configuration configuration, Registry registry) {
        return new SchemaManager(configuration, registry);
    }
}
