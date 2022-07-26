package com.devit.mscore.template.pebble;

import com.devit.mscore.Resource;
import com.devit.mscore.exception.ResourceException;

import java.io.File;

import org.json.JSONObject;

public class PebbleTemplateResource extends Resource {

  private static final String NAME_TEMPLATE = "%s.%s";

  protected PebbleTemplateResource(File resourceFile) throws ResourceException {
    super(resourceFile);
    var elements = resourceFile.getName().split("\\.");
    name = String.format(NAME_TEMPLATE, elements[0], elements[1]);
  }

  @Override
  public JSONObject getMessage() {
    return new JSONObject().put("name", name).put("content", content);
  }
}
