package com.devit.mscore.workflow.flowable;

import static com.devit.mscore.util.AttributeConstants.NAME;
import static com.devit.mscore.util.Constants.WORKFLOW;

import com.devit.mscore.WorkflowDefinition;
import com.devit.mscore.exception.ResourceException;

import java.io.File;

import org.json.JSONObject;

public class FlowableDefinition extends WorkflowDefinition {

  private String resourceName;

  /*
   * The file name should be: domain.action.version.bpmn20.xml
   */
  public FlowableDefinition(File definition) throws ResourceException {
    super(definition);
    var elements = definition.getName().split("\\.");
    name = String.format(NAME_TEMPLATE, elements[0], elements[1]);
    resourceName = String.format(RESOURCE_TEMPLATE, elements[0], elements[1], elements[2]);
  }

  public FlowableDefinition(JSONObject json) {
    super(json.getString(NAME), json.getString(CONTENT_CONSTANT));
    resourceName = json.getString(RESOURCE_NAME);
  }

  public FlowableDefinition(String content) {
    this(new JSONObject(content));
  }

  @Override
  public JSONObject getMessage(String definitionId) {
    return getMessage().put(WORKFLOW, definitionId);
  }

  @Override
  public JSONObject getMessage() {
    return new JSONObject().put(RESOURCE_NAME, resourceName).put(NAME, name).put(CONTENT_CONSTANT, content);
  }

  @Override
  public String getResourceName() {
    return resourceName;
  }
}
