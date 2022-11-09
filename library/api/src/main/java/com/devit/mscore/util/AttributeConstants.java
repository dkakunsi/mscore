package com.devit.mscore.util;

import static com.devit.mscore.util.JsonUtils.hasValue;

import org.json.JSONObject;

public interface AttributeConstants extends Constants {

  String NAME = "name";

  // TODO: remove this audit element. We already use history
  String CREATED_DATE = "createdDate";

  String CREATED_BY = "createdBy";

  String LAST_UPDATED_DATE = "lastUpdatedDate";

  String LAST_UPDATED_BY = "lastUpdatedBy";

  static boolean hasDomain(JSONObject json) {
    return hasValue(DOMAIN, json);
  }

  static String getDomain(JSONObject json) {
    return json.optString(DOMAIN);
  }

  static boolean hasId(JSONObject json) {
    return hasValue(ID, json);
  }

  static String getId(JSONObject json) {
    return json.optString(ID);
  }

  static boolean hasCode(JSONObject json) {
    return hasValue(CODE, json);
  }

  static String getCode(JSONObject json) {
    return json.optString(CODE);
  }

  static boolean hasName(JSONObject json) {
    return hasValue(NAME, json);
  }

  static String getName(JSONObject json) {
    return json.optString(NAME);
  }
}
