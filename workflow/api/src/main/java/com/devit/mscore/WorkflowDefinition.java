package com.devit.mscore;

import com.devit.mscore.exception.ResourceException;

import java.io.File;

import org.json.JSONObject;

/**
 * Workflow definition used for deployment.
 *
 * @author dkakunsi
 */
public abstract class WorkflowDefinition extends Resource {

  protected static final String CONTENT_CONSTANT = "content";

  protected static final String RESOURCE_NAME = "resourceName";

  protected static final String RESOURCE_TEMPLATE = "%s.%s.%s.bpmn20.xml";

  protected static final String NAME_TEMPLATE = "%s.%s";

  protected WorkflowDefinition(File resourceFile) throws ResourceException {
    super(resourceFile);
  }

  protected WorkflowDefinition(String name, String content) {
    super(name, content);
  }

  public abstract JSONObject getMessage(String definitionId);

  public abstract String getResourceName();
}
