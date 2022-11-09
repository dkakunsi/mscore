package com.devit.mscore.indexing.elasticsearch;

import com.devit.mscore.Resource;
import com.devit.mscore.exception.ResourceException;

import java.io.File;

import org.json.JSONObject;

public class ElasticsearchMapping extends Resource {

  protected ElasticsearchMapping(File resourceFile) throws ResourceException {
    super(resourceFile);
    name = resourceFile.getName().split("\\.")[0];
  }

  @Override
  public String getContent() {
    return new JSONObject(content).toString();
  }
}
