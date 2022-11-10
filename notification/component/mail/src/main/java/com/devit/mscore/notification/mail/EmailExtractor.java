package com.devit.mscore.notification.mail;

import com.devit.mscore.Logger;
import com.devit.mscore.logging.ApplicationLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;

public class EmailExtractor {

  private static final Logger LOGGER = ApplicationLogger.getLogger(EmailExtractor.class);

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

    for (var pa : possibleAttributes) {
      if (data.has(pa)) {
        var value = data.get(pa);
        if (value instanceof JSONObject) {
          nestedObjects.add((JSONObject) value);
        } else if (!(value instanceof JSONArray)) {
          possibility.add(value.toString());
        } else {
          LOGGER.info("Cannot use attribute '{}' of unsupported type '{}'", pa, value.getClass());
        }
      } else {
        var nested = data.keySet().stream()
            .map(k -> data.get(k))
            .filter(v -> v instanceof JSONObject)
            .map(v -> (JSONObject) v)
            .collect(Collectors.toList());
        nestedObjects.addAll(nested);
      }
    }

    // If empty look in the nested entity
    // PS: Only support 1 email per entity currently
    if (possibility.isEmpty()) {
      nestedObjects.forEach(no -> extract(no, possibility, possibleAttributes));
    }
  }
}
