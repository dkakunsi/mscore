package com.devit.mscore.filter;

import java.util.List;

import com.devit.mscore.Filter;

import org.json.JSONObject;

public class RemovingFilter extends Filter {

  RemovingFilter(List<String> attributes) {
    super(attributes);
  }

  @Override
  protected void apply(JSONObject json, String key) {
    json.remove(key);
  }
}
