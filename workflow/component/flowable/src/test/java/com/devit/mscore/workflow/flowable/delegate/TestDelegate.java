package com.devit.mscore.workflow.flowable.delegate;

import com.devit.mscore.Registry;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;

public class TestDelegate implements JavaDelegate {

  public static Registry registry;

  @Override
  public void execute(DelegateExecution execution) {
    registry.getName();
  }
}
