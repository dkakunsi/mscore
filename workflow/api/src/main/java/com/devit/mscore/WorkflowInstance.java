package com.devit.mscore;

import static com.devit.mscore.util.AttributeConstants.CREATED_BY;
import static com.devit.mscore.util.AttributeConstants.DOMAIN;
import static com.devit.mscore.util.AttributeConstants.ID;
import static com.devit.mscore.util.AttributeConstants.NAME;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

public abstract class WorkflowInstance extends WorkflowObject {

  private static final String ENTITY = "entity";

  private static final String ACTION = "action";

  public JSONObject toJson(List<WorkflowTask> tasks) {
    var taskArray = new JSONArray();
    tasks.forEach(task -> taskArray.put(task.toJson()));
    return toJson().put("task", taskArray);
  }

  @Override
  public JSONObject toJson() {
    var entity = new JSONObject();
    entity.put(ID, getBusinessKey());
    entity.put(DOMAIN, getDomain());

    var jsonObj = new JSONObject();
    jsonObj.put(ENTITY, entity);

    var ownerId = getOwner();
    if (StringUtils.isNotBlank(ownerId)) {
      jsonObj.put(OWNER, ownerId);
    }

    jsonObj.put(ORGANISATION, getOrganisation());
    jsonObj.put(CREATED_BY, getStartUserId());
    jsonObj.put(DOMAIN, "workflow");
    jsonObj.put(ID, getProcessInstanceId());
    jsonObj.put(ACTION, getAction());
    jsonObj.put(NAME, getName());
    jsonObj.put(STATUS_CONSTANT, this.status);

    return jsonObj;
  }

  protected abstract String getStartUserId();

  protected abstract String getBusinessKey();

	protected abstract String getOwner();

  protected abstract String getOrganisation();

  protected abstract String getDomain();

  protected abstract String getAction();

  protected abstract String getProcessInstanceId();

}
