package com.devit.mscore;

import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.devit.mscore.exception.ConfigException;

import java.util.List;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

public class FiltersExecutorTest {

  private Configuration configuration;

  @Before
  public void setup() {
    this.configuration = mock(Configuration.class);
    doReturn("data").when(this.configuration).getServiceName();
  }

  @Test
  public void testFilter() throws ConfigException {
    var attributes = List.of("password");
    var filter = new FilterImpl(attributes);
    var filterExecutor = new FiltersExecutor();
    filterExecutor.add(filter);

    var json = new JSONObject("{\"domain\":\"domain\",\"password\":\"abcd\"}");
    filterExecutor.execute(json);

    assertFalse(json.has("password"));
  }

  private static class FilterImpl extends Filter {

    protected FilterImpl(List<String> attributes) {
      super(attributes);
    }

    @Override
    protected void apply(JSONObject json, String key) {
      json.remove(key);
    }
  }
}
