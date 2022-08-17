package com.devit.mscore.data.filter;

import com.devit.mscore.Filter;

import java.util.List;

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
