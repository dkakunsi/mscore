package com.devit.mscore.notification.mail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;

public class EmailExtractor {

  private EmailExtractor() {
  }

  public static Optional<List<String>> extract(JSONObject data, List<String> possibleAttributes) {
    var possibility = new ArrayList<String>();
    extract(data, possibility, possibleAttributes);
    if (possibility.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(possibility);
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  private static void extract(JSONObject data, List<String> possibility, List<String> possibleAttributes) {
    var nestedObjects = new ArrayList<JSONObject>();

    for (var pa : possibleAttributes) {
      if (data.has(pa)) {
        var value = data.get(pa);
        if (value instanceof JSONObject) {
          nestedObjects.add((JSONObject) value);
        } else if (value instanceof JSONArray) {
          nestedObjects.addAll((List) ((JSONArray) value).toList());
        } else {
          possibility.add(value.toString());
        }
      } else {
        var nestedList = getJsonObjectsFromNestedArray(data);
        nestedObjects.addAll(nestedList);
        var nested = getNestedJsonObjects(data);
        nestedObjects.addAll(nested);
      }
    }

    // If empty look in the nested entity
    // PS: Only support 1 email per entity currently
    if (possibility.isEmpty()) {
      nestedObjects.forEach(no -> extract(no, possibility, possibleAttributes));
    }
  }

  @SuppressWarnings("rawtypes")
  private static List<JSONObject> getJsonObjectsFromNestedArray(JSONObject data) {
    return data.keySet()
        .stream()
        .map(k -> data.get(k))
        .filter(v -> v instanceof JSONArray)
        .map(v -> ((JSONArray) v).toList())
        .flatMap(Collection::stream)
        .filter(v -> v instanceof Map)
        .map(v -> new JSONObject((Map) v))
        .collect(Collectors.toList());
  }

  private static List<JSONObject> getNestedJsonObjects(JSONObject data) {
    return data.keySet().stream()
        .map(k -> data.get(k))
        .filter(v -> v instanceof JSONObject)
        .map(v -> (JSONObject) v)
        .collect(Collectors.toList());
  }
}
