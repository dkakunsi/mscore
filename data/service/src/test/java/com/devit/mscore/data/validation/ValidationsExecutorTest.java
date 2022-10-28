package com.devit.mscore.data.validation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.devit.mscore.Validation;
import com.devit.mscore.exception.ApplicationRuntimeException;
import com.devit.mscore.exception.ValidationException;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

public class ValidationsExecutorTest {

  private ValidationsExecutor executor;

  @Before
  public void setup() {
    this.executor = new ValidationsExecutor();
  }

  @Test
  public void testValidate() {
    var validation = mock(Validation.class);
    doReturn("all").when(validation).getDomain();
    doReturn(true).when(validation).validate(any(JSONObject.class));

    this.executor.add(validation);
    var json = new JSONObject("{\"domain\":\"domain\",\"field1\":\"value\",\"field2\":\"value\"}");
    this.executor.execute(json);
  }

  @Test
  public void testValidate_Failed() throws ValidationException {
    var validation = mock(Validation.class);
    doReturn("all").when(validation).getDomain();
    doReturn(false).when(validation).validate(any(JSONObject.class));

    this.executor.add(validation);
    var json = new JSONObject("{\"domain\":\"wrong\",\"field1\":\"value\",\"field2\":\"value\"}");
    var ex = assertThrows(ApplicationRuntimeException.class, () -> this.executor.execute(json));

    assertThat(ex.getCause(), instanceOf(ValidationException.class));
    assertThat(ex.getCause().getMessage(), is("The given data is not valid. Check the log for detail"));
  }

  @Test
  public void testValidate_NullJson() {
    var validation = mock(Validation.class);
    doReturn("all").when(validation).getDomain();

    this.executor.add(validation);
    var ex = assertThrows(ApplicationRuntimeException.class, () -> this.executor.execute((JSONObject) null));

    assertThat(ex.getCause(), instanceOf(ValidationException.class));
    assertThat(ex.getCause().getMessage(), is("The given data is not valid. Check the log for detail"));
  }
}
