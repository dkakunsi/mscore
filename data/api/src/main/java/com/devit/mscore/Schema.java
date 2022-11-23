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
    name = resourceFile.getName().split("\\.")[0];
  }

  protected Schema(String name, String content) {
    super(name, content);
  }

  public String getDomain() {
    return name;
  }

  public abstract void validate(JSONObject json) throws ValidationException;

  public abstract Map<String, List<String>> getReferences();

  public abstract Set<String> getReferenceNames();

  public abstract List<List<String>> getReferenceDomains();

  public abstract List<Index> getIndeces();

  public static class Index {

    private String field;

    private Options options;

    public Index(String field) {
      this.field = field;
    }

    public Index(String field, Options options) {
      this(field);
      this.options = options;
    }

    public String getField() {
      return field;
    }

    public Options getOptions() {
      return options;
    }

    public boolean isUnique() {
      return getOptions().isUnique();
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((field == null) ? 0 : field.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (!(obj instanceof Index))
        return false;
      Index other = (Index) obj;
      return field.equals(other.field);
    }

    public static class Options {

      private boolean unique;

      public Options(boolean unique) {
        this.unique = unique;
      }

      public boolean isUnique() {
        return unique;
      }
    }
  }
}
