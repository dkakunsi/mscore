package com.devit.mscore.data.observer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.devit.mscore.Index;
import com.devit.mscore.data.enrichment.EnrichmentsExecutor;
import com.devit.mscore.exception.IndexingException;

import org.json.JSONObject;
import org.junit.Test;

public class IndexingObserverTest {

  @Test
  public void testNotify_NullIndex() throws IndexingException {
    var index = mock(Index.class);
    var enricher = mock(EnrichmentsExecutor.class);
    var syncObserver = mock(SynchronizationObserver.class);
    var indexingObserver = new IndexingObserver(index, enricher, syncObserver, 0L);
    indexingObserver.notify(new JSONObject());

    verify(index).index(any(JSONObject.class));
  }

  @Test
  public void testNotify_ExceptionThrown() throws Exception {
    var index = mock(Index.class);
    var syncObserver = mock(SynchronizationObserver.class);
    var enricher = mock(EnrichmentsExecutor.class);
    doThrow(IndexingException.class).when(index).index(any(JSONObject.class));

    var indexingObserver = new IndexingObserver(index, enricher, syncObserver, 0L);
    indexingObserver.notify(new JSONObject());

    verify(index).index(any(JSONObject.class));
  }
}
