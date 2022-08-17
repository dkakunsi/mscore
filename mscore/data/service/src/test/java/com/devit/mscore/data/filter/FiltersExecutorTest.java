package com.devit.mscore.data.filter;

import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.devit.mscore.Configuration;
import com.devit.mscore.exception.ConfigException;

import java.util.Optional;

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
  public void testFilter_Password() throws ConfigException {
    doReturn(Optional.of("password")).when(this.configuration).getConfig("services.data.filter.remove");
    var factory = FilterFactory.of();
    var executors = factory.filters(this.configuration);
    var json = new JSONObject("{\"domain\":\"domain\",\"password\":\"abcd\"}");
    executors.execute(json);

    assertFalse(json.has("password"));
  }

  @Test
  public void testFilter_DocumentField() throws ConfigException {
    doReturn(Optional.of("document")).when(this.configuration).getConfig("services.data.filter.remove");
    var factory = FilterFactory.of();
    var executors = factory.filters(this.configuration);
    var json = new JSONObject("{\"domain\":\"domain\",\"document\":\"abcd\",\"documentation\":\"12345\"}");
    executors.execute(json);

    assertFalse(json.has("document"));
  }
}
