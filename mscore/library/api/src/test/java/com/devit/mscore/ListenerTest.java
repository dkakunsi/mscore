package com.devit.mscore;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.function.BiConsumer;

import com.devit.mscore.exception.ApplicationException;

import org.json.JSONObject;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class ListenerTest {

    @Test
    public void testListen() throws ApplicationException {
        var subscriber = mockSubscriber();
        var listener = listener(subscriber);
        var spiedListener = spy(listener);

        spiedListener.listen("topic1", "topic2");

        verify(spiedListener, times(2)).consume(any(ApplicationContext.class), any(JSONObject.class));

        verify(subscriber).start();
        spiedListener.stop();
        verify(subscriber).stop();
    }

    @Test
    public void testListen_StartError() throws ApplicationException {
        var subscriber = mock(Subscriber.class);
        doThrow(new ApplicationException("Cannot start consumer.")).when(subscriber).start();
        var listener = listener(subscriber);
        var spiedListener = spy(listener);

        spiedListener.listen("topic1", "topic2");
        verify(subscriber).start();
    }

    @SuppressWarnings("unchecked")
    private static Subscriber mockSubscriber() {
        var mockSubscriber = mock(Subscriber.class);
        doAnswer(new Answer<String>() {

            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                var consumer = (BiConsumer<ApplicationContext, JSONObject>) invocation.getArgument(1);
                consumer.accept(DefaultApplicationContext.of("test"), new JSONObject());
                return null;
            }

        }).when(mockSubscriber).subscribe(anyString(), any());
        return mockSubscriber;

    }

    private Listener listener(Subscriber subscriber) {
        return new Listener(subscriber) {

            @Override
            protected void consume(ApplicationContext context, JSONObject message) {
            }
        };        
    }
}
