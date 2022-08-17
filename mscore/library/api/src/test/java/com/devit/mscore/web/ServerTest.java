package com.devit.mscore.web;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

import java.util.List;

import com.devit.mscore.AuthenticationProvider;
import com.devit.mscore.exception.ApplicationException;
import com.devit.mscore.exception.ValidationException;

import org.junit.Before;
import org.junit.Test;

public class ServerTest {

  private Server server;

  @Before
  public void setup() {
    this.server = new Server(10, List.of()) {
      @Override
      public void start() {
      }

      @Override
      public void stop() {
      }
    };
  }

  @Test
  public void testCreateErrorMessage_WithInnerCauseMessage() {
    var statusCode = 400;
    var ex = new ApplicationException(new ValidationException("Validation exception"));
    var message = this.server.createResponseMessage(ex, statusCode);

    assertThat(message.getString("message"), is("Validation exception"));
    assertThat(message.getString("type"), is("REQUEST ERROR"));
  }

  @Test
  public void testCreateErrorMessage_WithOuterCauseMessage() {
    var statusCode = 500;
    var ex = new ApplicationException("Application exception", new ValidationException(""));
    var message = this.server.createResponseMessage(ex, statusCode);

    assertThat(message.getString("message"), is("Application exception"));
    assertThat(message.getString("type"), is("SERVER ERROR"));
  }

  @Test
  public void testCreateInformationMessage() {
    var statusCode = 100;
    var message = this.server.createResponseMessage("Informational message", statusCode);

    assertThat(message.getString("message"), is("Informational message"));
    assertThat(message.getString("type"), is("INFORMATION"));
  }

  @Test
  public void testCreateSuccessMessage() {
    var statusCode = 200;
    var message = this.server.createResponseMessage("Success message", statusCode);

    assertThat(message.getString("message"), is("Success message"));
    assertThat(message.getString("type"), is("SUCCESS"));
  }

  @Test
  public void testCreateRedirectionMessage() {
    var statusCode = 300;
    var message = this.server.createResponseMessage("Redirection message", statusCode);

    assertThat(message.getString("message"), is("Redirection message"));
    assertThat(message.getString("type"), is("REDIRECTION"));
  }

  @Test
  public void testCreateErrorMessage() {
    var statusCode = 600;
    var message = this.server.createResponseMessage("Error message", statusCode);

    assertThat(message.getString("message"), is("Error message"));
    assertThat(message.getString("type"), is("ERROR"));
  }

  @Test
  public void testSetterGetterAdder() {
    this.server.setAuthenticationProvider(mock(AuthenticationProvider.class));
    this.server.setValidations(List.of());

    assertNotNull(this.server.getAuthenticationProvider());
    assertNotNull(this.server.getValidations());
    assertNotEquals(0, this.server.getPort());
  }
}
