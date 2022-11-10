package com.devit.mscore.workflow.flowable.delegate;

import org.flowable.common.engine.api.delegate.Expression;
import org.flowable.engine.delegate.DelegateExecution;

public class SetStatus extends SetAttribute {

  // TODO: move to core constants
  private static final String STATUS = "status";

  private Expression status;

  @Override
  public void execute(DelegateExecution execution) {
    var statusValue = status.getValue(execution).toString();
    updateAttribute(execution, STATUS, statusValue);
  }
}
