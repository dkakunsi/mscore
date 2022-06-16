package com.devit.mscore;

import java.io.File;

import com.devit.mscore.exception.ResourceException;

import org.json.JSONObject;

/**
 * Workflow definition used for deployment.
 * 
 * @author dkakunsi
 */
public abstract class WorkflowDefinition extends Resource {

    protected WorkflowDefinition(File resourceFile) throws ResourceException {
        super(resourceFile);
    }

    protected WorkflowDefinition(String name, String content) {
        super(name, content);
    }

    public abstract JSONObject getMessage(String definitionId);

    public abstract String getResourceName();
}
