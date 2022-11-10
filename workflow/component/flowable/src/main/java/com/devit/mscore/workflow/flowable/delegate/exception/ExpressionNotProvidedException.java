package com.devit.mscore.workflow.flowable.delegate.exception;

import com.devit.mscore.exception.ApplicationRuntimeException;

public class ExpressionNotProvidedException extends ApplicationRuntimeException {

  public ExpressionNotProvidedException(String message) {
    super(message);
  }  
}
