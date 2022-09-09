package com.devit.mscore.workflow.flowable;

import com.devit.mscore.WorkflowTask;
import com.devit.mscore.WorkflowTaskRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.flowable.engine.HistoryService;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.TaskService;

public class FlowableTaskRepository implements WorkflowTaskRepository {

  private TaskService taskService;

  private HistoryService historyService;

  public FlowableTaskRepository(ProcessEngine processEngine) {
    this.taskService = processEngine.getTaskService();
    this.historyService = processEngine.getHistoryService();
  }

  @Override
  public List<WorkflowTask> getTasks(String instanceId) {
    var tasks = this.taskService.createTaskQuery().processInstanceId(instanceId).list();
    if (tasks == null) {
      return List.of();
    }

    return tasks.stream().map(task -> {
      var variables = getVariables(task.getProcessInstanceId());
      return new FlowableTask(task, variables);
    }).collect(Collectors.toList());
  }

  private Map<String, Object> getVariables(String instanceId) {
    var processInstance = this.historyService.createHistoricProcessInstanceQuery().processInstanceId(instanceId)
        .singleResult();
    return processInstance.getProcessVariables();
  }

  @Override
  public Optional<WorkflowTask> getTask(String taskId) {
    var task = this.taskService.createTaskQuery().taskId(taskId).singleResult();
    if (task == null) {
      return Optional.empty();
    }
    var variables = getVariables(task.getProcessInstanceId());
    return Optional.of(new FlowableTask(task, variables));
  }

  @Override
  public void complete(String taskId, Map<String, Object> variables) {
    this.taskService.complete(taskId, variables);    
  }
}
