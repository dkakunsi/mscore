package com.devit.mscore.data.observer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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

  @Test
  public void testNotify_NullPublisher() {
    var spiedObserver = spy(new PublishingObserver(null, 0L));
    spiedObserver.notify(new JSONObject());
    verify(spiedObserver).notify(any(JSONObject.class));
  }

  @Test
  public void testNotify_ExceptionThrown() {
    var contextData = new HashMap<String, Object>();
    contextData.put("action", "create");
    var context = DefaultApplicationContext.of("test", contextData);
    try (MockedStatic<ApplicationContext> utilities = Mockito.mockStatic(ApplicationContext.class)) {
      utilities.when(() -> ApplicationContext.getContext()).thenReturn(context);

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
}
