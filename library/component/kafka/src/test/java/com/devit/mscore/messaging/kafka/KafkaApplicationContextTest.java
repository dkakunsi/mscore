package com.devit.mscore.messaging.kafka;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.devit.mscore.Event;

import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.junit.Test;

public class KafkaApplicationContextTest {

  @Test
  public void test() {
    var principal = "{\"requestedBy\":\"requestedBy\"}";
    var principalHeader = mock(Header.class);
    doReturn(principal.getBytes()).when(principalHeader).value();

    var breadcrumbId = "breadcrumbId";
    var breadcrumbIdHeader = mock(Header.class);
    doReturn(breadcrumbId.getBytes()).when(breadcrumbIdHeader).value();

    var eventType = Event.Type.CREATE.name().toLowerCase();
    var eventTypeHeader = mock(Header.class);
    doReturn(eventType.getBytes()).when(eventTypeHeader).value();

    var headers = mock(Headers.class);
    doReturn(principalHeader).when(headers).lastHeader("principal");
    doReturn(breadcrumbIdHeader).when(headers).lastHeader("breadcrumbId");
    doReturn(eventTypeHeader).when(headers).lastHeader("eventType");

    var applicationContext = KafkaApplicationContext.of(headers);

    var expected = "requestedBy";
    assertThat(applicationContext.getRequestedBy(), is(expected));
    expected = principal;
    assertThat(applicationContext.getPrincipal().get().toString(), is(expected));
    expected = breadcrumbId;
    assertThat(applicationContext.getBreadcrumbId(), is(expected));
    expected = "{\"principal\":{\"requestedBy\":\"requestedBy\"},\"breadcrumbId\":\"breadcrumbId\",\"eventType\":\"create\"}";
    assertThat(applicationContext.toJson().toString(), is(expected));
    expected = eventType;
    assertThat(applicationContext.getEventType().get(), is(expected));

    expected = "messaging";
    assertThat(applicationContext.getSource(), is(expected));
  }

  @Test
  public void test_noPrincipal() {
    var headers = mock(Headers.class);
    doReturn(null).when(headers).lastHeader("principal");

    var applicationContext = KafkaApplicationContext.of(headers);

    var expected = "UNKNOWN";
    assertThat(applicationContext.getRequestedBy(), is(expected));

    assertNotNull(applicationContext.getBreadcrumbId());
  }
}
