package com.devit.mscore.enrichment;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Map;
import java.util.Optional;

import com.devit.mscore.Index;
import com.devit.mscore.exception.IndexingException;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

public class EnrichmentsExecutorTest {

  private EnrichmentsExecutor executor;

  @Before
  public void setup() {
    this.executor = new EnrichmentsExecutor();
  }

  @Test
  public void testEnrich_FromIndex() throws IndexingException {
    var index = mock(Index.class);
    var indeces = Map.of("enrichDomain", index);

    var enrichedJson = "{\"domain\":\"enrichDomain\",\"id\":\"enrichId\",\"name\":\"enrichName\"}";
    doReturn(Optional.of(new JSONObject(enrichedJson))).when(index).get("enrichId");
    var indexEnrichment = new IndexEnrichment(indeces, "domain", "attribute");
    var noopEnrichment = new NoopEnrichment("domain", "attribute2");
    this.executor.add(indexEnrichment);
    this.executor.add(noopEnrichment);

    var json = new JSONObject(
        "{\"domain\":\"domain\",\"id\":\"id\",\"attribute\":{\"domain\":\"enrichDomain\",\"id\":\"enrichId\"}}");
    this.executor.execute(json);

    verify(index, times(1)).get("enrichId");
    assertThat(json.getJSONObject("attribute").getString("domain"), is("enrichDomain"));
    assertThat(json.getJSONObject("attribute").getString("id"), is("enrichId"));
    assertThat(json.getJSONObject("attribute").getString("name"), is("enrichName"));
    assertThat(json.getJSONObject("attribute").length(), is(3));
  }

  @Test
  public void testEnrich_NoDomain() {
    var noopEnrichment = new NoopEnrichment("domain", "attribute");
    this.executor.add(noopEnrichment);

    var json = new JSONObject("{\"id\":\"id\",\"attribute\":{\"domain\":\"enrichDomain\",\"id\":\"enrichId\"}}");
    this.executor.execute(json);

    assertThat(json.getJSONObject("attribute").getString("domain"), is("enrichDomain"));
    assertThat(json.getJSONObject("attribute").getString("id"), is("enrichId"));
    assertThat(json.getJSONObject("attribute").length(), is(2));
  }

  @Test
  public void testEnrich_NoEnrichmentAvailable() {
    var json = new JSONObject(
        "{\"domain\":\"notavailable\",\"id\":\"id\",\"attribute\":{\"domain\":\"enrichDomain\",\"id\":\"enrichId\"}}");
    this.executor.execute(json);

    assertThat(json.getJSONObject("attribute").getString("domain"), is("enrichDomain"));
    assertThat(json.getJSONObject("attribute").getString("id"), is("enrichId"));
    assertThat(json.getJSONObject("attribute").length(), is(2));
  }

  @Test
  public void testEnrich_ThrowIndexingException() throws IndexingException {
    var index = mock(Index.class);
    var indeces = Map.of("enrichDomain", index);

    doThrow(new IndexingException("")).when(index).get("enrichId");
    var indexEnrichment = new IndexEnrichment(indeces, "domain", "attribute");
    var noopEnrichment = new NoopEnrichment("domain", "attribute2");
    this.executor.add(indexEnrichment);
    this.executor.add(noopEnrichment);

    var json = new JSONObject(
        "{\"domain\":\"domain\",\"id\":\"id\",\"attribute\":{\"domain\":\"enrichDomain\",\"id\":\"enrichId\"}}");
    this.executor.execute(json);

    verify(index, times(1)).get("enrichId");
    assertThat(json.getJSONObject("attribute").getString("domain"), is("enrichDomain"));
    assertThat(json.getJSONObject("attribute").getString("id"), is("enrichId"));
    assertThat(json.getJSONObject("attribute").length(), is(2));
  }
}
