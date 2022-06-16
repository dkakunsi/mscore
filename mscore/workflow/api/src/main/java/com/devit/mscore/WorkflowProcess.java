package com.devit.mscore;

import java.util.List;
import java.util.Map;

import com.devit.mscore.exception.ProcessException;

import org.json.JSONObject;

/**
 * Process interface to manage business process workflow.
 * 
 * @author dkakunsi
 */
public interface WorkflowProcess extends Starter {

    /**
     * Deploy process definition to process engine.
     * 
     * @param context            of the request.
     * @param workflowDefinition definition of the workflow.
     * @throws ProcessException error in process deployment
     */
    void deployDefinition(ApplicationContext context, WorkflowDefinition workflowDefinition) throws ProcessException;

    /**
     * Create instance of process definition.
     * 
     * @param context             of the request
     * @param processDefinitionId id
     * @param entity              object to process
     * @param variables           process variables
     * @return proxy of process instance
     * @throws ProcessException error in process update
     */
    WorkflowObject createInstance(ApplicationContext context, String processDefinitionId, JSONObject entity,
            Map<String, Object> variables) throws ProcessException;

    /**
     * Create instance for the specified {@code action}.
     * 
     * @param context   of the request
     * @param action    to applied
     * @param entity    object to process
     * @param variables process variables
     * @return proxy of process instance
     * @throws ProcessException error in process update
     */
    WorkflowObject createInstanceByAction(ApplicationContext context, String action, JSONObject entity,
            Map<String, Object> variables) throws ProcessException;

    /**
     * Retireve process instance by id.
     * 
     * @param context           of the request
     * @param processInstanceId id
     * @return proxy of process instance.
     */
    WorkflowObject getInstance(ApplicationContext context, String processInstanceId);

    /**
     * Retrieve list of tasks.
     * 
     * @param context           of the request
     * @param processInstanceId instance id.
     * @return list of task proxies.
     */
    List<WorkflowObject> getTasks(ApplicationContext context, String processInstanceId);

    /**
     * Complete the given task and continue the instance.
     * 
     * @param context      of the request
     * @param taskId       task id
     * @param taskResponse process variables
     * @throws ProcessException error in process update, mostly known issue is in
     *                          indexing the Workflow Task.
     */
    void completeTask(ApplicationContext context, String taskId, JSONObject taskResponse) throws ProcessException;
}
