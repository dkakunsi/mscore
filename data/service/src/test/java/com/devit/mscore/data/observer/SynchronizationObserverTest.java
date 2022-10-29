package com.devit.mscore.data.observer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.devit.mscore.data.synchronization.SynchronizationsExecutor;

import org.json.JSONObject;
import org.junit.Test;

public class SynchronizationObserverTest {
  
  @Test
  public void testNotify() throws CloneNotSupportedException {
    var executor = mock(SynchronizationsExecutor.class);
    var syncObserver = new SynchronizationObserver();
    syncObserver.setExecutor(executor);
    var message = new JSONObject();
    syncObserver.notify(message);
    verify(executor).execute(any(JSONObject.class));
  }
}
