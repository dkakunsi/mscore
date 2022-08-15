package com.devit.mscore.util;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Utility class for json processing.
 * 
 * @author dkakunsi
 */
public final class JsonUtils {

    private JsonUtils() {
    }

    /**
     * Copy all values from source into destination.
     * 
     * @param destination destination object.
     * @param source      source object.
     */
    public static void copy(JSONObject destination, JSONObject source) {
        if (source.isEmpty()) {
            return;
        }

        if (!destination.equals(source)) {
            for (var key : source.keySet()) {
                var value = source.get(key);
                if (isEmpty(value)) {
                    destination.remove(key);
                } else {
                    destination.put(key, value);
                }
            }
        }
    }

    private static boolean isEmpty(Object value) {
        return JSONObject.NULL.equals(value)
                || value instanceof String && (StringUtils.isBlank((String) value)) || "null".equals(value)
                || value instanceof JSONObject && ((JSONObject) value).isEmpty()
                || value instanceof JSONArray && ((JSONArray) value).isEmpty();
    }

    public static boolean isNotJsonString(String str) {
        return !isJsonString(str);
    }

    public static boolean isJsonString(String str) {
        return StringUtils.startsWithAny(str, "{", "[");
    }

    public static boolean hasValue(String key, JSONObject json) {
        return json.has(key) && !json.isNull(key);
    }

    public static JSONObject flatten(JSONObject objectToFlatten) {
        var target = new JSONObject();
        flatten(objectToFlatten, target, "");
        return target;
    }

    private static void flatten(JSONObject objectToFlatten, JSONObject target, String basePath) {
        objectToFlatten.keys().forEachRemaining(key -> {
            var value = objectToFlatten.get(key);
            flatten(value, target, createPath(basePath, key));
        });
    }

    private static void flatten(Object object, JSONObject target, String basePath) {
        if (object instanceof JSONObject) {
            flatten((JSONObject) object, target, basePath);
        } else if (object instanceof JSONArray) {
            int i = 0;
            for (var o : (JSONArray) object) {
                flatten(o, target, createPath(basePath, i));
                i++;
            }
        } else {
            target.put(basePath, object);
        }
    }

    private static String createPath(String basePath, Object currentPath) {
        if (StringUtils.isBlank(basePath)) {
            return currentPath.toString();
        }
        return String.format("%s_%s", basePath, currentPath);
    }
}
