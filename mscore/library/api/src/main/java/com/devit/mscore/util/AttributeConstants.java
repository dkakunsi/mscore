package com.devit.mscore.util;

import static com.devit.mscore.util.JsonUtils.hasValue;

import org.json.JSONObject;

public final class AttributeConstants {

    public static final String DOMAIN = "domain";

    public static final String ID = "id";

    public static final String CODE = "code";

    public static final String NAME = "name";

    public static final String CREATED_DATE = "createdDate";

    public static final String CREATED_BY = "createdBy";

    public static final String LAST_UPDATED_DATE = "lastUpdatedDate";

    public static final String LAST_UPDATED_BY = "lastUpdatedBy";

    private AttributeConstants() {
    }

    public static boolean hasDomain(JSONObject json) {
        return hasValue(DOMAIN, json);
    }

    public static String getDomain(JSONObject json) {
        return json.optString(DOMAIN);
    }

    public static boolean hasId(JSONObject json) {
        return hasValue(ID, json);
    }

    public static String getId(JSONObject json) {
        return json.optString(ID);
    }

    public static boolean hasCode(JSONObject json) {
        return hasValue(CODE, json);
    }

    public static String getCode(JSONObject json) {
        return json.optString(CODE);
    }

    public static boolean hasName(JSONObject json) {
        return hasValue(NAME, json);
    }

    public static String getName(JSONObject json) {
        return json.optString(NAME);
    }
}
