package com.devit.mscore.workflow.flowable;

import static com.devit.mscore.util.AttributeConstants.DOMAIN;

import com.devit.mscore.WorkflowInstance;

import java.util.Date;
import java.util.HashMap;
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
    this.status = processInstance.isEnded() ? COMPLETED : ACTIVATED;
    this.variables = new HashMap<>(variables);
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
    this.status = COMPLETED;
  }

  public boolean isComplete() {
    return COMPLETED.equals(this.status);
  }

  @Override
  protected String getOwner() {
    var owner = this.variables.get(OWNER);
    return owner != null ? owner.toString() : null;
  }

  @Override
  protected String getOrganisation() {
    var organisation = this.variables.get(ORGANISATION);
    return organisation != null ? organisation.toString() : null;
  }

  @Override
  protected String getDomain() {
    var domain = this.variables.get(DOMAIN);
    return domain != null ? domain.toString() : null;
  }

  @Override
  protected String getAction() {
    return this.getProcessDefinitionId().split(":")[0];
  }

  @Override
  public String toString() {
    return toJson().toString();
  }
}
