package com.devit.mscore;

import com.devit.mscore.exception.ResourceException;
import com.devit.mscore.exception.ValidationException;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONObject;

/**
 * Schema object to validate incoming JSON.
 *
 * @author dkakunsi
 */
public abstract class Schema extends Resource {

  protected Schema(File resourceFile) throws ResourceException {
    super(resourceFile);
    this.name = resourceFile.getName().split("\\.")[0];
  }

  protected Schema(String name, String content) {
    super(name, content);
  }

  public String getDomain() {
    return this.name;
  }

  public abstract void validate(JSONObject json) throws ValidationException;

  public abstract Map<String, List<String>> getReferences();

  public abstract Set<String> getReferenceNames();

  public abstract List<List<String>> getReferenceDomains();

  public abstract List<String> getUniqueAttributes();
}
