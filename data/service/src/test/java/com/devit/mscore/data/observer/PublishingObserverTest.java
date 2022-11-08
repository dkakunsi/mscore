package com.devit.mscore.data.observer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import com.devit.mscore.ApplicationContext;
import com.devit.mscore.DefaultApplicationContext;
import com.devit.mscore.Publisher;

import java.util.HashMap;

import org.json.JSONObject;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

public class PublishingObserverTest {

  private static final String PUBLISHING_CHANNEL = "channel1";

  @Test
  public void testNotify_NullPublisher() {
    var spiedObserver = spy(new PublishingObserver(null, PUBLISHING_CHANNEL));
    spiedObserver.notify(new JSONObject());
    verify(spiedObserver).notify(any(JSONObject.class));
  }

  @Test
  public void testNotify_ExceptionThrown() {
    var contextData = new HashMap<String, Object>();
    contextData.put("eventType", "create");
    var context = DefaultApplicationContext.of("test", contextData);
    try (MockedStatic<ApplicationContext> utilities = Mockito.mockStatic(ApplicationContext.class)) {
      utilities.when(() -> ApplicationContext.getContext()).thenReturn(context);

      var publisher = mock(Publisher.class);
      doThrow(new RuntimeException("Test")).when(publisher).publish(eq(PUBLISHING_CHANNEL), any(JSONObject.class));
  
      var publishingObserver = new PublishingObserver(publisher, PUBLISHING_CHANNEL);
  
      var json = new JSONObject();
      var ex = assertThrows(RuntimeException.class, () -> {
        publishingObserver.notify(json);
      });
      assertThat("Test", is(ex.getMessage()));

      verify(publisher).publish(eq(PUBLISHING_CHANNEL), any(JSONObject.class));
    }
  }
}
