package com.devit.mscore.notification.mail;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

  private static void extract(JSONObject data, List<String> possibility, List<String> possibleAttributes) {
    var nestedObjects = new ArrayList<JSONObject>();
    for (var key : data.keySet()) {
      var value = data.get(key);
      if (possibleAttributes.contains(key)) {
        possibility.add((String) value);
      } else if (data.get(key) instanceof JSONObject) {
        nestedObjects.add((JSONObject) value);
      }
    }

    // If empty look in the nested entity
    if (possibility.isEmpty()) {
      for (var nestedObject : nestedObjects) {
        extract(nestedObject, possibility, possibleAttributes);
      }
    }
  }
}
