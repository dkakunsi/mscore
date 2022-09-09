package com.devit.mscore.workflow.flowable;

import com.devit.mscore.WorkflowInstance;
import com.devit.mscore.WorkflowInstanceRepository;

import java.util.Map;
import java.util.Optional;

import org.flowable.engine.HistoryService;
import org.flowable.engine.IdentityService;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.RuntimeService;

public class FlowableInstanceRepository implements WorkflowInstanceRepository {

  private final RuntimeService runtimeService;

  private final HistoryService historyService;

  private final IdentityService identityService;

  public FlowableInstanceRepository(ProcessEngine processEngine) {
    this.runtimeService = processEngine.getRuntimeService();
    this.historyService = processEngine.getHistoryService();
    this.identityService = processEngine.getIdentityService();
  }

  @Override
  public WorkflowInstance create(String definitionId, Map<String, Object> variables) {
    var createdBy = variables.get("createdBy").toString();
    this.identityService.setAuthenticatedUserId(createdBy);

    var businessKey = variables.get("businessKey").toString();
    var name = variables.get("name").toString();
    var processInstance = this.runtimeService.createProcessInstanceBuilder()
        .processDefinitionId(definitionId)
        .businessKey(businessKey)
        .name(name)
        .variables(variables)
        .start();
    var processVariables = getVariables(processInstance.getId());
    return new FlowableInstance(processInstance, processVariables);
  }

  @Override
  public Map<String, Object> getVariables(String instanceId) {
    return this.runtimeService.getVariables(instanceId);
  }

  @Override
  public Optional<WorkflowInstance> get(String instanceId) {
    var processInstance = this.runtimeService.createProcessInstanceQuery().processInstanceId(instanceId)
        .singleResult();
    if (processInstance == null) {
      return Optional.empty();
    }
    var processInstanceVariables = getVariables(processInstance.getId());
    return Optional.of(new FlowableInstance(processInstance, processInstanceVariables));
  }

  @Override
  public boolean isCompleted(String instanceId) {
    var historicProcessInstance = this.historyService.createHistoricProcessInstanceQuery()
        .processInstanceId(instanceId).finished().singleResult();

    return historicProcessInstance != null;
  }
}
