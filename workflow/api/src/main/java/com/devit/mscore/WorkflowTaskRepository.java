package com.devit.mscore;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface WorkflowTaskRepository {
  
  List<WorkflowTask> getTasks(String instanceId);

  Optional<WorkflowTask> getTask(String taskId);

  void complete(String taskId, Map<String, Object> map);
}
