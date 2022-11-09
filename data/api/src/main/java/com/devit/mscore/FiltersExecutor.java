package com.devit.mscore;

import static com.devit.mscore.util.AttributeConstants.getDomain;
import static com.devit.mscore.util.Constants.ALL;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

/**
 * <p>
 * Mediate the execution of {@code filters}. It will use it on demand.
 * </p>
 *
 * @author dkakunsi
 */
public final class FiltersExecutor implements Executor<Filter> {

  private Map<String, List<Filter>> filters;

  public FiltersExecutor() {
    filters = new HashMap<>();
  }

  @Override
  public void add(Filter filter) {
    var domain = filter.getDomain();
    filters.computeIfAbsent(domain, key -> new ArrayList<>());
    filters.get(domain).add(filter);
  }

  @Override
  public void execute(JSONObject json) {
    filter(filters.get(ALL), json);
    filter(filters.get(getDomain(json)), json);
  }

  private static void filter(List<Filter> filters, JSONObject json) {
    if (filters == null || json == null) {
      return;
    }
    filters.forEach(filter -> filter.filter(json));
  }
}
