package com.devit.mscore.workflow.flowable.delegate;

import org.flowable.engine.delegate.DelegateExecution;

public class SetStatus extends SetAttribute {

  // TODO: move to core constants
  private static final String STATUS = "status";

  @Override
  public void execute(DelegateExecution execution) {
    initContext(execution);

    logger.info("Set entity status");

    var status = getValue(execution);
    updateAttribute(execution, STATUS, status);
  }
}
