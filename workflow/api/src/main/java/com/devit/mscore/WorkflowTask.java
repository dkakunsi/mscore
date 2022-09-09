package com.devit.mscore;

import static com.devit.mscore.util.AttributeConstants.ID;
import static com.devit.mscore.util.AttributeConstants.NAME;
import static com.devit.mscore.util.DateUtils.toZonedDateTime;

import java.util.Date;

import org.json.JSONObject;

public abstract class WorkflowTask extends WorkflowObject {

  private static final String DUE_DATE = "dueDate";

  private static final String ASSIGNEE = "assignee";

  private static final String EXECUTION_ID = "executionId";

  @Override
  public JSONObject toJson() {
    var json = new JSONObject();
    json.put(ID, getId());
    json.put(NAME, getName());
    json.put(DUE_DATE, toZonedDateTime(getDueDate()));
    json.put(ASSIGNEE, getAssignee());
    json.put(ORGANISATION, getOrganisation());
    json.put(EXECUTION_ID, getExecutionId());
    json.put(OWNER, getOwner());
    json.put(STATUS_CONSTANT, this.status);
    return json;
  }

  protected abstract String getOwner();

  protected abstract String getExecutionId();

  protected abstract String getOrganisation();

  protected abstract String getAssignee();

  protected abstract Date getDueDate();

  public abstract String getInstanceId();
}
