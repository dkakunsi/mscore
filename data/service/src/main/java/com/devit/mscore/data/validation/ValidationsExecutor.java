package com.devit.mscore.data.validation;

import static com.devit.mscore.util.AttributeConstants.getDomain;
import static com.devit.mscore.util.Constants.ALL;

import com.devit.mscore.Executor;
import com.devit.mscore.Validation;
import com.devit.mscore.exception.ApplicationRuntimeException;
import com.devit.mscore.exception.ValidationException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

/**
 * <p>
 * Mediate the execution of {@code validations}. It will execute it on demand.
 * </p>
 *
 * @author dkakunsi
 */
public class ValidationsExecutor implements Executor<Validation> {

  private static final String INVALID_DATA = "The given data is not valid. Check the log for detail";

  private Map<String, List<Validation>> validations;

  public ValidationsExecutor() {
    validations = new HashMap<>();
  }

  @Override
  public void add(Validation validation) {
    var domain = validation.getDomain();
    validations.computeIfAbsent(domain, key -> new ArrayList<>());
    validations.get(domain).add(validation);
  }

  @Override
  public void execute(JSONObject json) {
    if (!valid(validations.get(ALL), json) || !valid(validations.get(getDomain(json)), json)) {
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
