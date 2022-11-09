package com.devit.mscore.data.observer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.devit.mscore.Index;
import com.devit.mscore.data.synchronization.IndexSynchronization;
import com.devit.mscore.exception.IndexingException;

import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

public class SynchronizationObserverTest {

  @Test
  public void testNotify() throws IndexingException {
    var index = mock(Index.class);
    doReturn("domain").when(index).getName();
    var arr = new JSONArray();
    arr.put(new JSONObject());
    doReturn(Optional.of(arr)).when(index).search(any(JSONObject.class));

    var syncObserver = new SynchronizationObserver();
    var indexSync = new IndexSynchronization(index, "domain", "attribute");
    syncObserver.add(indexSync);

    assertThat(indexSync.getDomain(), is("domain"));

    var message = new JSONObject();
    message.put("id", "id");
    message.put("code", "code");
    message.put("name", "name");
    message.put("domain", "domain");

    syncObserver.notify(message);
    verify(index).search(any(JSONObject.class));
  }
}
