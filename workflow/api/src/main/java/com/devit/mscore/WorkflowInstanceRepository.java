package com.devit.mscore;

import java.util.Map;
import java.util.Optional;

public interface WorkflowInstanceRepository {

  WorkflowInstance create(String definitionId, Map<String, Object> variables);

  Map<String, Object> getVariables(String instanceId);

  Optional<WorkflowInstance> get(String instanceId);

  boolean isCompleted(String instanceId);
}
