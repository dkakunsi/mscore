package com.devit.mscore.workflow.flowable;

import static com.devit.mscore.util.Constants.DOMAIN;

import com.devit.mscore.WorkflowInstance;

import java.util.Date;
import java.util.Map;

import org.flowable.engine.runtime.ProcessInstance;

/**
 * Flowable ProcessInstance proxy.
 *
 * @author dkakunsi
 */
public class FlowableInstance extends WorkflowInstance implements ProcessInstance {

  private ProcessInstance processInstance;

  private Map<String, Object> variables;

  public FlowableInstance(ProcessInstance processInstance, Map<String, Object> variables) {
    this.processInstance = processInstance;
    this.variables = variables;
    status = processInstance.isEnded() ? COMPLETED : ACTIVATED;
  }

  @Override
  public String getId() {
    return processInstance.getId();
  }

  @Override
  public boolean isEnded() {
    return processInstance.isEnded();
  }

  @Override
  public String getActivityId() {
    return processInstance.getActivityId();
  }

  @Override
  public String getProcessInstanceId() {
    return processInstance.getProcessInstanceId();
  }

  @Override
  public String getParentId() {
    return processInstance.getParentId();
  }

  @Override
  public String getSuperExecutionId() {
    return processInstance.getSuperExecutionId();
  }

  @Override
  public String getRootProcessInstanceId() {
    return processInstance.getRootProcessInstanceId();
  }

  @Override
  public String getReferenceId() {
    return processInstance.getReferenceId();
  }

  @Override
  public String getReferenceType() {
    return processInstance.getReferenceType();
  }

  @Override
  public String getPropagatedStageInstanceId() {
    return processInstance.getPropagatedStageInstanceId();
  }

  @Override
  public String getProcessDefinitionId() {
    return processInstance.getProcessDefinitionId();
  }

  @Override
  public String getProcessDefinitionName() {
    return processInstance.getProcessDefinitionName();
  }

  @Override
  public String getProcessDefinitionKey() {
    return processInstance.getProcessDefinitionKey();
  }

  @Override
  public Integer getProcessDefinitionVersion() {
    return processInstance.getProcessDefinitionVersion();
  }

  @Override
  public String getDeploymentId() {
    return processInstance.getDeploymentId();
  }

  @Override
  public String getBusinessKey() {
    return processInstance.getBusinessKey();
  }

  @Override
  public boolean isSuspended() {
    return processInstance.isSuspended();
  }

  @Override
  public Map<String, Object> getProcessVariables() {
    return processInstance.getProcessVariables();
  }

  @Override
  public String getTenantId() {
    return processInstance.getTenantId();
  }

  @Override
  public String getName() {
    return processInstance.getName();
  }

  @Override
  public String getDescription() {
    return processInstance.getDescription();
  }

  @Override
  public String getLocalizedName() {
    return processInstance.getLocalizedName();
  }

  @Override
  public String getLocalizedDescription() {
    return processInstance.getLocalizedDescription();
  }

  @Override
  public Date getStartTime() {
    return processInstance.getStartTime();
  }

  @Override
  public String getStartUserId() {
    return processInstance.getStartUserId();
  }

  @Override
  public String getCallbackId() {
    return processInstance.getCallbackId();
  }

  @Override
  public String getCallbackType() {
    return processInstance.getCallbackType();
  }

  @Override
  public void complete() {
    status = COMPLETED;
  }

  public boolean isComplete() {
    return COMPLETED.equals(status);
  }

  @Override
  protected String getOwner() {
    var owner = variables.get(OWNER);
    return owner != null ? owner.toString() : null;
  }

  @Override
  protected String getOrganisation() {
    var organisation = variables.get(ORGANISATION);
    return organisation != null ? organisation.toString() : null;
  }

  @Override
  protected String getDomain() {
    var domain = variables.get(DOMAIN);
    return domain != null ? domain.toString() : null;
  }

  @Override
  protected String getAction() {
    return getProcessDefinitionId().split(":")[0];
  }

  @Override
  public String toString() {
    return toJson().toString();
  }
}
