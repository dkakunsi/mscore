package com.devit.mscore.validation;

import static com.devit.mscore.util.AttributeConstants.getDomain;
import static com.devit.mscore.util.Utils.ALL;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.devit.mscore.Executor;
import com.devit.mscore.Validation;
import com.devit.mscore.exception.ApplicationRuntimeException;
import com.devit.mscore.exception.ValidationException;

import org.json.JSONObject;

/**
 * <p>
 * Mediate the execution of {@code validations}. It will execute it on demand.
 * </p>
 *
 * @author dkakunsi
 */
public class ValidationsExecutor implements Executor<Validation> {

  private static final String INVALID_DATA = "The given data is not valid. Check the log for detail.";

  private Map<String, List<Validation>> validations;

  public ValidationsExecutor() {
    this.validations = new HashMap<>();
  }

  @Override
  public void add(Validation validation) {
    var domain = validation.getDomain();
    this.validations.computeIfAbsent(domain, key -> new ArrayList<>());
    this.validations.get(domain).add(validation);
  }

  @Override
  public void execute(JSONObject json) {
    if (!valid(this.validations.get(ALL), json) || !valid(this.validations.get(getDomain(json)), json)) {
      throw new ApplicationRuntimeException(new ValidationException(INVALID_DATA));
    }
  }

  private static boolean valid(List<Validation> validations, JSONObject json) {
    if (validations == null) {
      return true;
    }
    if (json == null) {
      return false;
    }
    return validations.stream().allMatch(v -> v.validate(json));
  }
}
