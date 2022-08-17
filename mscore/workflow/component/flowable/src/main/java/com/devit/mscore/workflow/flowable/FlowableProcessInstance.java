package com.devit.mscore.workflow.flowable;

import static com.devit.mscore.util.AttributeConstants.DOMAIN;
import static com.devit.mscore.util.AttributeConstants.ID;

import java.util.Date;
import java.util.Map;

import com.devit.mscore.WorkflowObject;

import org.apache.commons.lang3.StringUtils;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.runtime.ProcessInstance;
import org.json.JSONObject;

/**
 * Flowable ProcessInstance proxy.
 * 
 * @author dkakunsi
 */
public class FlowableProcessInstance implements WorkflowObject, ProcessInstance {

  private static final String STATUS = "status";

  private static final String OWNER = "owner";

  private static final String ORGANISATION = "organisation";

  private ProcessInstance processInstance;

  private RuntimeService runtimeService;

  private JSONObject json;

  private String instanceStatus;

  public FlowableProcessInstance(RuntimeService runtimeService, ProcessInstance processInstance) {
    this.runtimeService = runtimeService;
    this.processInstance = processInstance;
    this.instanceStatus = processInstance.isEnded() ? COMPLETED : ACTIVATED;
    this.json = initJson();
  }

  private JSONObject initJson() {
    var entity = new JSONObject();
    entity.put(ID, getBusinessKey());
    entity.put(DOMAIN, getDomain());

    var jsonObj = new JSONObject();
    jsonObj.put("entity", entity);

    var ownerId = getOwner();
    if (StringUtils.isNotBlank(ownerId)) {
      jsonObj.put(OWNER, ownerId);
    }

    jsonObj.put(ORGANISATION, getOrganisation());
    jsonObj.put("createdBy", getStartUserId());
    jsonObj.put(DOMAIN, "workflow");
    jsonObj.put(ID, getProcessInstanceId());
    jsonObj.put("action", getAction());
    jsonObj.put("name", getName());
    jsonObj.put(STATUS, instanceStatus);

    return jsonObj;
  }

  @Override
  public String getId() {
    return this.processInstance.getId();
  }

  @Override
  public boolean isEnded() {
    return this.processInstance.isEnded();
  }

  @Override
  public String getActivityId() {
    return this.processInstance.getActivityId();
  }

  @Override
  public String getProcessInstanceId() {
    return this.processInstance.getProcessInstanceId();
  }

  @Override
  public String getParentId() {
    return this.processInstance.getParentId();
  }

  @Override
  public String getSuperExecutionId() {
    return this.processInstance.getSuperExecutionId();
  }

  @Override
  public String getRootProcessInstanceId() {
    return this.processInstance.getRootProcessInstanceId();
  }

  @Override
  public String getReferenceId() {
    return this.processInstance.getReferenceId();
  }

  @Override
  public String getReferenceType() {
    return this.processInstance.getReferenceType();
  }

  @Override
  public String getPropagatedStageInstanceId() {
    return this.processInstance.getPropagatedStageInstanceId();
  }

  @Override
  public String getProcessDefinitionId() {
    return this.processInstance.getProcessDefinitionId();
  }

  @Override
  public String getProcessDefinitionName() {
    return this.processInstance.getProcessDefinitionName();
  }

  @Override
  public String getProcessDefinitionKey() {
    return this.processInstance.getProcessDefinitionKey();
  }

  @Override
  public Integer getProcessDefinitionVersion() {
    return this.processInstance.getProcessDefinitionVersion();
  }

  @Override
  public String getDeploymentId() {
    return this.processInstance.getDeploymentId();
  }

  @Override
  public String getBusinessKey() {
    return this.processInstance.getBusinessKey();
  }

  @Override
  public boolean isSuspended() {
    return this.processInstance.isSuspended();
  }

  @Override
  public Map<String, Object> getProcessVariables() {
    return this.processInstance.getProcessVariables();
  }

  @Override
  public String getTenantId() {
    return this.processInstance.getTenantId();
  }

  @Override
  public String getName() {
    return this.processInstance.getName();
  }

  @Override
  public String getDescription() {
    return this.processInstance.getDescription();
  }

  @Override
  public String getLocalizedName() {
    return this.processInstance.getLocalizedName();
  }

  @Override
  public String getLocalizedDescription() {
    return this.processInstance.getLocalizedDescription();
  }

  @Override
  public Date getStartTime() {
    return this.processInstance.getStartTime();
  }

  @Override
  public String getStartUserId() {
    return this.processInstance.getStartUserId();
  }

  @Override
  public String getCallbackId() {
    return this.processInstance.getCallbackId();
  }

  @Override
  public String getCallbackType() {
    return this.processInstance.getCallbackType();
  }

  @Override
  public void complete() {
    this.instanceStatus = COMPLETED;
  }

  public boolean isComplete() {
    return COMPLETED.equals(this.instanceStatus);
  }

  @Override
  public JSONObject toJson() {
    this.json.put(STATUS, instanceStatus);
    return this.json;
  }

  private String getOwner() {
    var owner = getVariables().get(OWNER);
    return owner != null ? owner.toString() : null;
  }

  private String getOrganisation() {
    var organisation = getVariables().get(ORGANISATION);
    return organisation != null ? organisation.toString() : null;
  }

  private String getDomain() {
    var domain = getVariables().get(DOMAIN);
    return domain != null ? domain.toString() : null;
  }

  private Map<String, Object> getVariables() {
    return this.runtimeService.getVariables(getId());
  }

  private String getAction() {
    return this.getProcessDefinitionId().split(":")[0];
  }

  @Override
  public String toString() {
    return toJson().toString();
  }
}
