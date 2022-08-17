package com.devit.mscore.observer;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.devit.mscore.Publisher;

import org.json.JSONObject;
import org.junit.Test;

public class PublishingObserverTest {

  @Test
  public void testNotify_NullPublisher() {
    var publisher = mock(Publisher.class);
    var publishingObserver = new PublishingObserver(publisher, 0L);
    publishingObserver.notify(new JSONObject());

    verify(publisher).publish(any(JSONObject.class));
  }

  @Test
  public void testNotify_ExceptionThrown() {
    var publisher = mock(Publisher.class);
    doThrow(new RuntimeException("Test")).when(publisher).publish(any(JSONObject.class));

    var publishingObserver = new PublishingObserver(publisher, 0L);

    var json = new JSONObject();
    var ex = assertThrows(RuntimeException.class, () -> {
      publishingObserver.notify(json);
    });

    verify(publisher).publish(any(JSONObject.class));
    assertThat("Test", is(ex.getMessage()));
  }
}
