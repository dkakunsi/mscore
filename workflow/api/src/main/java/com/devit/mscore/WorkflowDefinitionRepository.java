package com.devit.mscore;

import java.util.Optional;

/**
 * Manage definition in database.
 */
public interface WorkflowDefinitionRepository {

  /**
   * Deploy the definition to the underlaying workflow engine.
   * 
   * @param definition of workflow
   */
  void deploy(WorkflowDefinition definition);

  /**
   * Check if the specified {@code} definition} already in database.
   * 
   * @param definition of workflow
   * @return true if exists
   */
  boolean isExists(WorkflowDefinition definition);

  Optional<String> getDefinitionId(String resourceName);
}
