package com.devit.mscore;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.devit.mscore.exception.ApplicationRuntimeException;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

public class ExecutorTest {

  @Test
  public void testExecute_Array() {
    var executor = new ExecutorImpl();
    var spiedExecutor = spy(executor);

    var jsons = new JSONArray();
    jsons.put(new JSONObject());
    jsons.put(new JSONObject());
    jsons.put(new JSONObject());
    assertThat(jsons.length(), is(3));

    spiedExecutor.execute(jsons);

    verify(spiedExecutor, times(3)).execute(any(JSONObject.class));
  }

  public class ExecutorImpl implements Executor<Object> {

    @Override
    public void add(Object object) {
    }

    @Override
    public void execute(JSONObject json) throws ApplicationRuntimeException {
    }
  }
}
