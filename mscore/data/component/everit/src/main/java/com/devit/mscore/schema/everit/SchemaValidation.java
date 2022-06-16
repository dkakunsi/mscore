package com.devit.mscore.schema.everit;

import static com.devit.mscore.util.AttributeConstants.DOMAIN;
import static com.devit.mscore.util.AttributeConstants.getCode;
import static com.devit.mscore.util.AttributeConstants.hasDomain;

import com.devit.mscore.ApplicationContext;
import com.devit.mscore.Registry;
import com.devit.mscore.Validation;
import com.devit.mscore.exception.ApplicationRuntimeException;
import com.devit.mscore.exception.RegistryException;
import com.devit.mscore.exception.ValidationException;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Validate json object to it's {@code schema}.
 * 
 * <p>
 * <i>This filter applies to all domains.</i>
 * </p>
 * 
 * @author dkakunsi
 */
public class SchemaValidation implements Validation {

    private static final Logger LOG = LoggerFactory.getLogger(SchemaValidation.class);

    private Registry registry;

    public SchemaValidation(Registry registry) {
        this.registry = registry;
    }

    @Override
    public boolean validate(ApplicationContext context, JSONObject json) {
        if (!hasDomain(json)) {
            var cause = new ValidationException("Invalid data. No domain found.");
            throw new ApplicationRuntimeException(cause);
        }

        var domain = json.getString(DOMAIN);
        LOG.debug("BreadcrumbId: {}. Validating {}", context.getBreadcrumbId(), domain);

        try {
            var registeredSchema = this.registry.get(context, domain);
            new JSONSchema(new JSONObject(registeredSchema)).validate(json);
            return true;
        } catch (ValidationException ex) {
            LOG.error("BreadcrumbId: {}. Validation failed for object: {}", context.getBreadcrumbId(), getCode(json), ex);
            return false;
        } catch (RegistryException ex) {
            LOG.error("BreadcrumbId: {}. Cannot validate since the schema is not exist: {}", context.getBreadcrumbId(), domain);
            throw new ApplicationRuntimeException(ex);
        }
    }
}
