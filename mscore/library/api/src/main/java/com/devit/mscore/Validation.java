package com.devit.mscore;

import static com.devit.mscore.util.Utils.ALL;

import org.json.JSONObject;

/**
 * Root for validation process.
 * 
 * @author dkakunsi
 */
public interface Validation {

    /**
     * 
     * @return domain this validation applies to.
     */
    default String getDomain() {
        return ALL;
    }

    /**
     * <p>
     * Validate json object.
     * </p>
     * 
     * @param context application context.
     * @param object  to validate.
     * @return true when object is valid, else otherwise.
     */
    boolean validate(ApplicationContext context, JSONObject json);
}