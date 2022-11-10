package com.devit.mscore.workflow.flowable.delegate;

import static com.devit.mscore.WorkflowConstants.ORGANISATION;

import org.flowable.engine.delegate.DelegateExecution;

public class SetOrganisationVariable extends ApplicationDelegate {

  @Override
  public void execute(DelegateExecution execution) {
    initContext(execution);

    logger.info("Initializing organization variable");

    var organisation = getValue(execution);
    execution.setVariable(ORGANISATION, organisation);
  }
}
