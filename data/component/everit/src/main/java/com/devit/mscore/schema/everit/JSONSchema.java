package com.devit.mscore.schema.everit;

import static com.devit.mscore.util.Constants.DOMAIN;

import com.devit.mscore.Schema;
import com.devit.mscore.exception.ResourceException;
import com.devit.mscore.exception.ValidationException;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONArray;
import org.json.JSONObject;

public class JSONSchema extends Schema {

  private static final String PROPERTIES = "properties";

  private static final String INDEX = "index";

  private static final String CONST = "const";

  private static final String ONE_OF = "oneOf";

  private Map<String, List<String>> references;

  JSONSchema(File resourceFile) throws ResourceException {
    super(resourceFile);
    references = new HashMap<>();

    init();
  }

  JSONSchema(JSONObject json) {
    this(json.getString("name"), json.getString("content"));
  }

  private JSONSchema(String name, String content) {
    super(name, content);
    references = new HashMap<>();

    init();
  }

  public static JSONSchema of(File resourceFile) throws ResourceException {
    return new JSONSchema(resourceFile);
  }

  public static JSONSchema of(JSONObject json) {
    return new JSONSchema(json);
  }

  private void init() {
    var json = new JSONObject(content).getJSONObject(PROPERTIES);

    for (var key : json.keySet()) {
      var domains = getReferenceDomain(json.get(key));
      if (domains.isPresent()) {
        references.put(key, domains.get());
      }
    }
  }

  private Optional<List<String>> getReferenceDomain(Object attribute) {
    if (!(attribute instanceof JSONObject)) {
      return Optional.empty();
    }

    var json = (JSONObject) attribute;
    var domain = getReferenceDomain(json);
    if (domain.isPresent()) {
      return domain;
    }

    domain = getNullableReferenceDomain(json);
    if (domain.isPresent()) {
      return domain;
    }

    return Optional.empty();
  }

  private Optional<List<String>> getReferenceDomain(JSONObject json) {
    if (!json.has(PROPERTIES)) {
      return Optional.empty();
    }

    List<String> domains = new ArrayList<>();
    var properties = json.getJSONObject(PROPERTIES);
    if (properties.has(DOMAIN)) {
      var domain = properties.getJSONObject(DOMAIN);
      if (domain.has(CONST)) {
        domains.add(domain.getString(CONST));
      } else if (domain.has(ONE_OF)) {
        domains = getMultipleDomains(domain.getJSONArray(ONE_OF));
      }
    }

    return Optional.of(domains);
  }

  private List<String> getMultipleDomains(JSONArray array) {
    var domains = new ArrayList<String>();
    for (var object : array) {
      var json = (JSONObject) object;
      if (json.has(CONST)) {
        domains.add(json.getString(CONST));
      }
    }
    return domains;
  }

  private Optional<List<String>> getNullableReferenceDomain(JSONObject json) {
    if (!json.has(ONE_OF)) {
      return Optional.empty();
    }

    var array = json.getJSONArray(ONE_OF);
    for (var object : array) {
      var value = getReferenceDomain((JSONObject) object);
      if (value.isPresent()) {
        return value;
      }
    }

    return Optional.empty();
  }

  @Override
  public void validate(JSONObject json) throws ValidationException {
    try {
      var jsonSchema = new JSONObject(getContent());
      var loader = SchemaLoader.builder().schemaJson(jsonSchema).draftV7Support().build();
      var everitSchema = loader.load().build();
      everitSchema.validate(json);
    } catch (org.everit.json.schema.ValidationException ex) {
      throw new ValidationException("Failed to validate JSON", ex);
    }
  }

  @Override
  public Map<String, List<String>> getReferences() {
    return new HashMap<>(references);
  }

  @Override
  public Set<String> getReferenceNames() {
    return references.keySet();
  }

  @Override
  public List<List<String>> getReferenceDomains() {
    var values = references.values();
    return new ArrayList<>(values);
  }

  @Override
  public List<Index> getIndeces() {
    var schema = getJsonSchema();
    var indexDefinition = schema.getJSONArray(INDEX);

    var indeces = new ArrayList<Index>();
    indexDefinition.forEach(i -> {
      var json = (JSONObject) i;
      var field = json.getString("field");
      var isUnique = json.getBoolean("unique");
      indeces.add(new Index(field, new Index.Options(isUnique)));
    });
    return indeces;
  }

  private JSONObject getJsonSchema() {
    return new JSONObject(content);
  }
}
