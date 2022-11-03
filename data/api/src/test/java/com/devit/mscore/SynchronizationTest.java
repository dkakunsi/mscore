package com.devit.mscore;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import com.devit.mscore.exception.SynchronizationException;

import java.util.HashMap;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

public class SynchronizationTest {

  private static final String REFERENCE_ATTRIBUTE = "referenceAttribute";

  private static final String REFERENCE_DOMAIN = "referenceDomain";

  private static final String SEARCH_ATTRIBUTE = REFERENCE_ATTRIBUTE + ".id";

  private FiltersExecutor filtersExecutor;

  private PostProcessObserver postProcessObserver;

  private JSONObject json;

  @Before
  public void setup() {
    filtersExecutor = new FiltersExecutor();
    postProcessObserver = mock(PostProcessObserver.class);

    json = new JSONObject();
    json.put("id", "id");
    json.put("domain", "domain");
    json.put("code", "code");
  }

  @Test
  public void test() {
    var map = spy(new HashMap<String, JSONObject>());
    var synchronization = new SynchronizationTestImpl(REFERENCE_DOMAIN, REFERENCE_ATTRIBUTE, map);
    assertThat(synchronization.getReferenceDomain(), is(REFERENCE_DOMAIN));
    assertThat(synchronization.getReferenceAttribute(), is(REFERENCE_ATTRIBUTE));
    assertThat(synchronization.getSearchAttribute(), is(SEARCH_ATTRIBUTE));
  }

  @Test
  public void givenDataIsAvailable_WhenSynchIsRequested_ShouldCallRetrieveAndPersist() throws SynchronizationException {
    var map = spy(new HashMap<String, JSONObject>());
    doReturn(json).when(map).get(anyString());

    var synchronization = new SynchronizationTestImpl(REFERENCE_DOMAIN, REFERENCE_ATTRIBUTE, map)
        .with(filtersExecutor)
        .with(postProcessObserver);
    synchronization.synchronize();

    verify(map).get(anyString());
  }

  @Test
  public void givenDataIsAvailable_WhenSyncByIdIsRequested_ShouldRetrieveAndPersistData() throws SynchronizationException {
    var map = spy(new HashMap<String, JSONObject>());
    doReturn(json).when(map).get(anyString());

    var synchronization = new SynchronizationTestImpl(REFERENCE_DOMAIN, REFERENCE_ATTRIBUTE, map)
        .with(filtersExecutor)
        .with(postProcessObserver);
    synchronization.synchronize("id");

    verify(map).get(anyString());
  }

  @Test
  public void givenDataIsNotAvailable_WhenSyncByIdIsRequested_ShouldRetrieveAndNotPersistData() throws SynchronizationException {
    var map = spy(new HashMap<String, JSONObject>());
    doReturn(null).when(map).get(anyString());

    var synchronization = new SynchronizationTestImpl(REFERENCE_DOMAIN, REFERENCE_ATTRIBUTE, map)
        .with(filtersExecutor)
        .with(postProcessObserver);
    synchronization.synchronize("id");

    verify(map).get(anyString());
  }

  @Test
  public void givenExceptionIsThrown_WhenSyncIsRequested_ShouldNotError() throws SynchronizationException {
    var map = spy(new HashMap<String, JSONObject>());
    doThrow(RuntimeException.class).when(map).put(anyString(), any(JSONObject.class));


    var synchronization = new SynchronizationTestImpl(REFERENCE_DOMAIN, REFERENCE_ATTRIBUTE, map)
        .with(filtersExecutor)
        .with(postProcessObserver);
    synchronization.synchronize("id");

    verify(map).get(anyString());
  }
}
