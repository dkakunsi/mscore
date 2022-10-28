package com.devit.mscore.workflow.flowable.delegate;

import com.devit.mscore.Logger;
import com.devit.mscore.logging.ApplicationLogger;

import org.flowable.common.engine.api.delegate.Expression;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;

public class InitializeProcess implements JavaDelegate {

  private static final Logger LOG = ApplicationLogger.getLogger(InitializeProcess.class);

  private Expression organisation;

  @Override
  public void execute(DelegateExecution execution) {
    try {
      var organisationValue = this.organisation.getValue(execution).toString();
      execution.setVariable("organisation", organisationValue);
    } catch (NullPointerException ex) {
      LOG.warn("No organisation is provided");
    }
  }
}
