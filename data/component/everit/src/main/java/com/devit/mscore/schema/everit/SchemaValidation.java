package com.devit.mscore.schema.everit;

import static com.devit.mscore.util.AttributeConstants.getCode;
import static com.devit.mscore.util.AttributeConstants.hasDomain;
import static com.devit.mscore.util.Constants.DOMAIN;

import com.devit.mscore.Logger;
import com.devit.mscore.Registry;
import com.devit.mscore.Validation;
import com.devit.mscore.exception.ApplicationRuntimeException;
import com.devit.mscore.exception.RegistryException;
import com.devit.mscore.exception.ValidationException;
import com.devit.mscore.logging.ApplicationLogger;

import org.json.JSONObject;

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

  private static final Logger LOG = ApplicationLogger.getLogger(SchemaValidation.class);

  private Registry registry;

  public SchemaValidation(Registry registry) {
    this.registry = registry;
  }

  @Override
  public boolean validate(JSONObject json) {
    if (!hasDomain(json)) {
      var cause = new ValidationException("No domain found in object");
      throw new ApplicationRuntimeException(cause);
    }

    var domain = json.getString(DOMAIN);
    LOG.debug("Validating object of domain '{}'", domain);

    try {
      var registeredSchema = registry.get(domain);
      new JSONSchema(new JSONObject(registeredSchema)).validate(json);
      return true;
    } catch (ValidationException ex) {
      LOG.error("Validation failed for object '{}' of domain '{}'", ex, getCode(json), domain);
      return false;
    } catch (RegistryException ex) {
      LOG.error("Cannot validate object since the schema '{}' is not exist", domain);
      throw new ApplicationRuntimeException(ex);
    }
  }
}
