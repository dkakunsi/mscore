package com.devit.mscore.messaging.kafka;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

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

    var headers = mock(Headers.class);
    doReturn(principalHeader).when(headers).lastHeader("principal");
    doReturn(breadcrumbIdHeader).when(headers).lastHeader("breadcrumbId");

    var applicationContext = KafkaApplicationContext.of(headers);

    var expected = "requestedBy";
    assertThat(applicationContext.getRequestedBy(), is(expected));
    expected = principal;
    assertThat(applicationContext.getPrincipal().get().toString(), is(expected));
    expected = breadcrumbId;
    assertThat(applicationContext.getBreadcrumbId(), is(expected));
    expected = "{\"principal\":{\"requestedBy\":\"requestedBy\"},\"breadcrumbId\":\"breadcrumbId\"}";
    assertThat(applicationContext.toJson().toString(), is(expected));
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
