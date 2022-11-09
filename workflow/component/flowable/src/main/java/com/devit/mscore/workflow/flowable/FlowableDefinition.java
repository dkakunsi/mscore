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
    this.name = String.format(NAME_TEMPLATE, elements[0], elements[1]);
    this.resourceName = String.format(RESOURCE_TEMPLATE, elements[0], elements[1], elements[2]);
  }

  public FlowableDefinition(JSONObject json) {
    super(json.getString(NAME), json.getString(CONTENT_CONSTANT));
    this.resourceName = json.getString(RESOURCE_NAME);
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
    return new JSONObject().put(RESOURCE_NAME, this.resourceName).put(NAME, this.name).put(CONTENT_CONSTANT, this.content);
  }

  @Override
  public String getResourceName() {
    return this.resourceName;
  }
}
