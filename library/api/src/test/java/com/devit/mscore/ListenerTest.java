package com.devit.mscore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.devit.mscore.exception.ApplicationException;

import java.util.function.Consumer;

import org.json.JSONObject;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class ListenerTest {

  @Test
  public void testListen() throws ApplicationException {
    var subscriber = mockSubscriber();
    var logger = mock(Logger.class);
    var listener = listener(subscriber, logger);
    var spiedListener = spy(listener);

    spiedListener.listen("topic1", "topic2");

    verify(spiedListener, times(2)).consume(any(JSONObject.class));

    verify(subscriber).start();
    spiedListener.stop();
    verify(subscriber).stop();
  }

  @Test
  public void testListen_StartError() throws ApplicationException {
    var subscriber = mock(Subscriber.class);
    doThrow(new ApplicationException("Cannot start consumer")).when(subscriber).start();
    var logger = mock(Logger.class);
    var listener = listener(subscriber, logger);
    var spiedListener = spy(listener);

    var ex = assertThrows(ApplicationException.class, () -> spiedListener.listen("topic1", "topic2"));
    assertEquals("Cannot start consumer", ex.getMessage());
    verify(subscriber).start();
  }

  @SuppressWarnings("unchecked")
  private static Subscriber mockSubscriber() {
    var mockSubscriber = mock(Subscriber.class);
    doAnswer(new Answer<String>() {

      @Override
      public String answer(InvocationOnMock invocation) throws Throwable {
        var consumer = (Consumer<JSONObject>) invocation.getArgument(1);
        consumer.accept(new JSONObject());
        return null;
      }

    }).when(mockSubscriber).subscribe(anyString(), any());
    return mockSubscriber;

  }

  private Listener listener(Subscriber subscriber, Logger logger) {
    return new Listener(subscriber, logger) {

      @Override
      public void consume(JSONObject message) {
      }
    };
  }
}
