package com.devit.mscore.data.observer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.devit.mscore.Index;
import com.devit.mscore.Index.SearchCriteria;
import com.devit.mscore.data.synchronization.IndexSynchronization;
import com.devit.mscore.exception.IndexingException;

import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

public class SynchronizationObserverTest {

  private Index index;

  private SynchronizationObserver syncObserver;

  private IndexSynchronization indexSync;

  private JSONObject message;

  @Before
  public void setup() {
    index = mock(Index.class);
    syncObserver = new SynchronizationObserver();
    indexSync = new IndexSynchronization(index, "domain", "attribute");
    syncObserver.add(indexSync);

    doReturn("domain").when(index).getName();
    assertThat(indexSync.getDomain(), is("domain"));
    assertThrows(RuntimeException.class, () -> indexSync.getSchema());

    message = new JSONObject();
    message.put("id", "id");
    message.put("code", "code");
    message.put("name", "name");
    message.put("domain", "domain");
  }

  @Test
  public void testNotify() throws IndexingException {
    var arr = new JSONArray();
    arr.put(new JSONObject());
    doReturn(Optional.of(arr)).when(index).search(any(SearchCriteria.class));

    syncObserver.notify(message);
    verify(index).search(any(SearchCriteria.class));
  }

  @Test
  public void testNotify_WhenEmpty() throws IndexingException {
    doReturn(Optional.empty()).when(index).search(any(SearchCriteria.class));

    syncObserver.notify(message);
    verify(index).search(any(SearchCriteria.class));
  }

  @Test
  public void testNotify_WhenException() throws IndexingException {
    doThrow(IndexingException.class).when(index).search(any(SearchCriteria.class));

    syncObserver.notify(message);
    verify(index).search(any(SearchCriteria.class));
  }
}
