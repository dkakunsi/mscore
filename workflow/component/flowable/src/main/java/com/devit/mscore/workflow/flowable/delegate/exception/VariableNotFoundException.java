package com.devit.mscore.workflow.flowable.delegate.exception;

import com.devit.mscore.exception.ApplicationRuntimeException;

public class VariableNotFoundException extends ApplicationRuntimeException {

  public VariableNotFoundException(String message) {
    super(message);
  }  
}
