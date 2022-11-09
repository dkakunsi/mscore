package com.devit.mscore;

import com.devit.mscore.exception.ConfigException;

public interface WorkflowDataSource<T> {

  enum Type {
    SQL
  }

  Type getType();

  T get() throws ConfigException;
}
