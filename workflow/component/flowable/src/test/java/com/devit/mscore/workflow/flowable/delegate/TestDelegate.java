package com.devit.mscore.workflow.flowable.delegate;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;

public class TestDelegate implements JavaDelegate {

  @Override
  public void execute(DelegateExecution execution) {
    execution.getCurrentActivityId();
  }
}
