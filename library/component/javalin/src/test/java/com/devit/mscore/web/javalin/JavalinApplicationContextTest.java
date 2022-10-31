package com.devit.mscore.web.javalin;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import org.junit.Test;

import io.javalin.http.Context;

public class JavalinApplicationContextTest {

  @Test
  public void testDataAvailable() {
    var principal = "{\"requestedBy\":\"requestedBy\",\"role\":[\"user\"]}";
    var breadcrumbId = "breadcrumbId";
    var action = "domain.action";
    var ctx = mock(Context.class);
    doReturn(principal).when(ctx).header("principal");
    doReturn(breadcrumbId).when(ctx).header("breadcrumbId");
    doReturn(action).when(ctx).header("action");

    var applicationContext = JavalinApplicationContext.of(ctx);

    var expected = "requestedBy";
    assertThat(applicationContext.getRequestedBy(), is(expected));
    expected = "breadcrumbId";
    assertThat(applicationContext.getBreadcrumbId(), is(expected));
    expected = principal;
    assertThat(applicationContext.getPrincipal().get().toString(), is(expected));

    assertTrue(applicationContext.hasRole("user"));
    assertFalse(applicationContext.hasRole("admin"));
  }

  @Test
  public void testDataNotAvailable() {
    var ctx = mock(Context.class);
    doReturn(null).when(ctx).header("principal");
    doReturn(null).when(ctx).header("breadcrumbId");
    doReturn(null).when(ctx).header("eventType");

    var applicationContext = JavalinApplicationContext.of(ctx);

    assertThat(applicationContext.getRequestedBy(), is("UNKNOWN"));
    assertTrue(applicationContext.getEventType().isEmpty());
    assertTrue(applicationContext.getPrincipal().isEmpty());
    assertNotNull(applicationContext.getBreadcrumbId());
    assertFalse(applicationContext.hasRole("user"));
  }
}
