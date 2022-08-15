package com.devit.mscore.synchronization;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.devit.mscore.Synchronizer;
import com.devit.mscore.exception.SynchronizationException;

import org.json.JSONObject;
import org.junit.Test;

public class SynchronizationListenerTest {

    @Test
    public void test() throws SynchronizationException {
        var synchronizer = mock(Synchronizer.class);
        var syncExecutor = new SynchronizationsExecutor();
        var spiedSynchronization = spy(new DefaultSynchronization(synchronizer, "referenceDomain", "referenceAttribute"));
        syncExecutor.add(spiedSynchronization);

        var listener = new SynchronizationListener(null, syncExecutor);
        listener.consume(new JSONObject("{\"domain\":\"referenceDomain\",\"id\":\"id\"}"));

        verify(spiedSynchronization, times(1)).synchronize("id");
    }
}
