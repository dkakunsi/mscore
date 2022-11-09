package com.devit.mscore;

import static com.devit.mscore.util.Constants.ALL;

import java.util.List;

import org.json.JSONObject;

/**
 * Interface to filter unneeded data before process it.
 *
 * @author dkakunsi
 */
public abstract class Filter {

  protected List<String> attributes;

  protected Filter(List<String> attributes) {
    this.attributes = attributes;
  }

  /**
   *
   * @return domain this filter applies to.
   */
  public String getDomain() {
    return ALL;
  }

  /**
   * Filter the given json.
   *
   * @param json to filter.
   */
  public void filter(JSONObject json) {
    this.attributes.forEach(attribute -> apply(json, attribute));
  }

  protected abstract void apply(JSONObject json, String key);
}
