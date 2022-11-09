package com.devit.mscore.workflow.flowable;

import com.devit.mscore.Logger;
import com.devit.mscore.WorkflowDefinition;
import com.devit.mscore.WorkflowDefinitionRepository;
import com.devit.mscore.logging.ApplicationLogger;

import java.util.Optional;

import org.flowable.engine.ProcessEngine;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.ProcessDefinition;

public class FlowableDefinitionRepository implements WorkflowDefinitionRepository {

  private static final Logger LOGGER = ApplicationLogger.getLogger(FlowableDefinitionRepository.class);

  private RepositoryService repositoryService;

  public FlowableDefinitionRepository(ProcessEngine processEngine) {
    repositoryService = processEngine.getRepositoryService();
  }

  @Override
  public void deploy(WorkflowDefinition definition) {
    if (!isExists(definition)) {
      repositoryService.createDeployment().addString(definition.getResourceName(), definition.getContent())
          .deploy();
      LOGGER.debug("Definition '{}' is deployed to Flowable", definition.getName());
    }
  }

  @Override
  public boolean isExists(WorkflowDefinition definition) {
    return getDefinition(definition.getResourceName()).isPresent();
  }

  @Override
  public Optional<String> getDefinitionId(String resourceName) {
    var definition = getDefinition(resourceName);
    return definition.isEmpty() ? Optional.empty() : Optional.of(definition.get().getId());
  }

  private Optional<ProcessDefinition> getDefinition(String resourceName) {
    var definitions = repositoryService.createProcessDefinitionQuery().processDefinitionResourceName(resourceName)
        .list();
    return definitions == null || definitions.isEmpty() ? Optional.empty() : Optional.of(definitions.get(0));
  }
}
