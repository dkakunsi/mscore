package com.devit.mscore.workflow.flowable;

import com.devit.mscore.WorkflowTask;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.flowable.identitylink.api.IdentityLinkInfo;
import org.flowable.task.api.TaskInfo;

public class FlowableTask extends WorkflowTask implements TaskInfo {

  private TaskInfo task;

  private Map<String, Object> variables;

  public FlowableTask(TaskInfo task, Map<String, Object> variables) {
    this.task = task;
    this.status = ACTIVATED;
    this.variables = new HashMap<>(variables);
  }

  @Override
  public String getName() {
    return this.task.getName();
  }

  @Override
  public String getDescription() {
    return this.task.getDescription();
  }

  @Override
  public int getPriority() {
    return this.task.getPriority();
  }

  @Override
  public String getOwner() {
    return this.task.getOwner();
  }

  @Override
  public String getAssignee() {
    return this.task.getAssignee();
  }

  @Override
  public String getProcessInstanceId() {
    return this.task.getProcessInstanceId();
  }

  @Override
  public String getExecutionId() {
    return this.task.getExecutionId();
  }

  @Override
  public String getTaskDefinitionId() {
    return this.task.getTaskDefinitionId();
  }

  @Override
  public String getProcessDefinitionId() {
    return this.task.getProcessDefinitionId();
  }

  @Override
  public String getScopeId() {
    return this.task.getScopeId();
  }

  @Override
  public String getSubScopeId() {
    return this.task.getSubScopeId();
  }

  @Override
  public String getScopeType() {
    return this.task.getScopeType();
  }

  @Override
  public String getScopeDefinitionId() {
    return this.task.getScopeDefinitionId();
  }

  @Override
  public String getPropagatedStageInstanceId() {
    return this.task.getPropagatedStageInstanceId();
  }

  @Override
  public Date getCreateTime() {
    return this.task.getCreateTime();
  }

  @Override
  public String getTaskDefinitionKey() {
    return this.task.getTaskDefinitionKey();
  }

  @Override
  public Date getDueDate() {
    return this.task.getDueDate();
  }

  @Override
  public String getCategory() {
    return this.task.getCategory();
  }

  @Override
  public String getParentTaskId() {
    return this.task.getParentTaskId();
  }

  @Override
  public String getTenantId() {
    return this.task.getTenantId();
  }

  @Override
  public String getFormKey() {
    return this.task.getFormKey();
  }

  @Override
  public Map<String, Object> getTaskLocalVariables() {
    return this.task.getTaskLocalVariables();
  }

  @Override
  public Map<String, Object> getProcessVariables() {
    return this.task.getProcessVariables();
  }

  @Override
  public List<? extends IdentityLinkInfo> getIdentityLinks() {
    return this.task.getIdentityLinks();
  }

  @Override
  public Date getClaimTime() {
    return this.task.getClaimTime();
  }

  @Override
  public String getId() {
    return this.task.getId();
  }

  @Override
  protected String getOrganisation() {
    var organisation = this.variables.get(ORGANISATION);
    return organisation != null ? organisation.toString() : null;
  }

  @Override
  public String toString() {
    return toJson().toString();
  }

  @Override
  public String getInstanceId() {
    return getProcessInstanceId();
  }
}
