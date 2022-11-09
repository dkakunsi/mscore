package com.devit.mscore.workflow.flowable;

import com.devit.mscore.WorkflowTask;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.flowable.identitylink.api.IdentityLinkInfo;
import org.flowable.task.api.TaskInfo;

public class FlowableTask extends WorkflowTask implements TaskInfo {

  private TaskInfo task;

  private Map<String, Object> variables;

  public FlowableTask(TaskInfo task, Map<String, Object> variables) {
    this.task = task;
    this.variables = variables;
    status = ACTIVATED;
  }

  @Override
  public String getName() {
    return task.getName();
  }

  @Override
  public String getDescription() {
    return task.getDescription();
  }

  @Override
  public int getPriority() {
    return task.getPriority();
  }

  @Override
  public String getOwner() {
    return task.getOwner();
  }

  @Override
  public String getAssignee() {
    return task.getAssignee();
  }

  @Override
  public String getProcessInstanceId() {
    return task.getProcessInstanceId();
  }

  @Override
  public String getExecutionId() {
    return task.getExecutionId();
  }

  @Override
  public String getTaskDefinitionId() {
    return task.getTaskDefinitionId();
  }

  @Override
  public String getProcessDefinitionId() {
    return task.getProcessDefinitionId();
  }

  @Override
  public String getScopeId() {
    return task.getScopeId();
  }

  @Override
  public String getSubScopeId() {
    return task.getSubScopeId();
  }

  @Override
  public String getScopeType() {
    return task.getScopeType();
  }

  @Override
  public String getScopeDefinitionId() {
    return task.getScopeDefinitionId();
  }

  @Override
  public String getPropagatedStageInstanceId() {
    return task.getPropagatedStageInstanceId();
  }

  @Override
  public Date getCreateTime() {
    return task.getCreateTime();
  }

  @Override
  public String getTaskDefinitionKey() {
    return task.getTaskDefinitionKey();
  }

  @Override
  public Date getDueDate() {
    return task.getDueDate();
  }

  @Override
  public String getCategory() {
    return task.getCategory();
  }

  @Override
  public String getParentTaskId() {
    return task.getParentTaskId();
  }

  @Override
  public String getTenantId() {
    return task.getTenantId();
  }

  @Override
  public String getFormKey() {
    return task.getFormKey();
  }

  @Override
  public Map<String, Object> getTaskLocalVariables() {
    return task.getTaskLocalVariables();
  }

  @Override
  public Map<String, Object> getProcessVariables() {
    return task.getProcessVariables();
  }

  @Override
  public List<? extends IdentityLinkInfo> getIdentityLinks() {
    return task.getIdentityLinks();
  }

  @Override
  public Date getClaimTime() {
    return task.getClaimTime();
  }

  @Override
  public String getId() {
    return task.getId();
  }

  @Override
  protected String getOrganisation() {
    var organisation = variables.get(ORGANISATION);
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
