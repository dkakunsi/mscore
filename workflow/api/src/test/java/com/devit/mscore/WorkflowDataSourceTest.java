package com.devit.mscore;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import com.devit.mscore.exception.ConfigException;

import org.junit.Test;

public class WorkflowDataSourceTest {

  @Test
  public void test() {
    var dataSource = new DummyDataSource();
    assertThat(dataSource.getType(), is(WorkflowDataSource.Type.SQL));
  }

  private class DummyDataSource implements WorkflowDataSource<Object> {

    @Override
    public Type getType() {
      return Type.SQL;
    }

    @Override
    public Object get() throws ConfigException {
      return null;
    }
  }
}
