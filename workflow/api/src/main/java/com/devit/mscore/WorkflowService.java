package com.devit.mscore;

import com.devit.mscore.exception.ProcessException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.json.JSONObject;

/**
 * Process interface to manage business process workflow.
 *
 * @author dkakunsi
 */
public interface WorkflowService extends Service {

  /**
   * Deploy process definition to process engine.
   *
   * @param workflowDefinition definition of the workflow.
   * @throws ProcessException error in process deployment
   */
  void deployDefinition(WorkflowDefinition workflowDefinition) throws ProcessException;

  /**
   * Create instance for the specified {@code action}.
   *
   * @param action    to applied
   * @param entity    object to process
   * @param variables process variables
   * @return proxy of process instance
   * @throws ProcessException error in process update
   */
  Optional<WorkflowInstance> executeWorkflow(String action, JSONObject entity,
      Map<String, Object> variables) throws ProcessException;

  /**
   * Retireve process instance by id.
   *
   * @param processInstanceId id
   * @return proxy of process instance.
   */
  Optional<WorkflowInstance> getInstance(String processInstanceId);

  /**
   * Retrieve list of tasks.
   *
   * @param processInstanceId instance id.
   * @return list of task proxies.
   */
  List<WorkflowTask> getTasks(String processInstanceId);

  /**
   * Complete the given task and continue the instance.
   *
   * @param taskId    task id
   * @param variables process variables
   * @throws ProcessException error in process update, mostly known issue is in
   *                          indexing the Workflow Task.
   */
  void completeTask(String taskId, Map<String, Object> variables) throws ProcessException;
}
