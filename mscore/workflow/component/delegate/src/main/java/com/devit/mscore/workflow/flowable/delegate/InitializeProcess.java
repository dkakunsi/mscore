package com.devit.mscore.workflow.flowable.delegate;

import org.flowable.common.engine.api.delegate.Expression;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;

public class InitializeProcess implements JavaDelegate {

  private Expression organisation;

  @Override
  public void execute(DelegateExecution execution) {
    if (this.organisation == null) {
      return;
    }

    var organisationValue = this.organisation.getValue(execution).toString();
    execution.setVariable("organisation", organisationValue);
  }
}
