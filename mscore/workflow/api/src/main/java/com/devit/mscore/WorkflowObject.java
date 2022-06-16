package com.devit.mscore;

import org.json.JSONObject;

/**
 * Base interface for workflow proxy object.
 * 
 * @author dkakunsi
 */
public interface WorkflowObject {

    String ACTIVATED = "Active";

    String COMPLETED = "Complete";

    String getId();

    String getName();

    void complete();

    default JSONObject toJson() {
        return new JSONObject();
    }
}
