package com.devit.mscore.template.pebble;

import java.io.File;

import com.devit.mscore.Resource;
import com.devit.mscore.exception.ResourceException;

import org.json.JSONObject;

public class PebbleTemplateResource extends Resource {

  private static final String NAME_TEMPLATE = "%s.%s";

  protected PebbleTemplateResource(File resourceFile) throws ResourceException {
    super(resourceFile);
    var elements = resourceFile.getName().split("\\.");
    this.name = String.format(NAME_TEMPLATE, elements[0], elements[1]);
  }

  @Override
  public JSONObject getMessage() {
    return new JSONObject().put("name", this.name).put("content", this.content);
  }
}
