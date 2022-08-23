package com.devit.mscore;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;

import com.devit.mscore.exception.DataException;
import com.devit.mscore.exception.EnrichmentException;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

public class EnrichmentTest {

  private static final String ATTRIBUTE_TO_ENRICH = "reference";

  private Enrichment enrichment;

  private Enrichment failRetryEnrichment;

  @Before
  public void setup() {
    this.enrichment = new Enrichment(ATTRIBUTE_TO_ENRICH) {
      @Override
      protected Optional<JSONObject> loadFromDataStore(String domain, String id) throws DataException {
        if (StringUtils.equals(id, "throwexception")) {
          throw new DataException("");
        }
        var reference = new JSONObject();
        reference.put("domain", domain);
        reference.put("id", id);
        reference.put("keyA", "Value A");
        reference.put("keyB", "Value B");
        reference.put("keyC", "Value C");

        return Optional.of(reference);
      }
    };

    this.failRetryEnrichment = new Enrichment(ATTRIBUTE_TO_ENRICH) {
      @Override
      protected Optional<JSONObject> loadFromDataStore(String domain, String id) throws DataException {
        return Optional.empty();
      }

    };

    assertThat(this.enrichment.getDomain(), is("all"));
    assertThat(this.enrichment.getAttribute(), is(ATTRIBUTE_TO_ENRICH));
  }

  @Test
  public void testEnrich_JsonObject_Reference() throws EnrichmentException {
    var json = new JSONObject();
    json.put(ATTRIBUTE_TO_ENRICH, createJsonObject("referencedomain", "referenceid"));

    this.enrichment.enrich(json);
    var enriched = json.getJSONObject(ATTRIBUTE_TO_ENRICH);
    assertResult(enriched);
  }

  @Test
  public void testEnrich_JsonObject_Reference_NotFound() throws EnrichmentException {
    var json = new JSONObject();
    json.put(ATTRIBUTE_TO_ENRICH, createJsonObject("referencedomain", "referenceid"));

    this.failRetryEnrichment.enrich(json);
    var enriched = json.getJSONObject(ATTRIBUTE_TO_ENRICH);
    assertThat(enriched.length(), is(3));
  }

  @Test
  public void testEnrich_JsonObject_References() throws EnrichmentException {
    var reference = createJsonObject("referencedomain", "referenceid");
    var references = new JSONArray();
    references.put(reference);

    var json = createJsonObject("domain", "id");
    json.put(ATTRIBUTE_TO_ENRICH, references);

    this.enrichment.enrich(json);
    var enriched = json.getJSONArray(ATTRIBUTE_TO_ENRICH).getJSONObject(0);
    assertResult(enriched);
  }

  @Test
  public void testEnrich_InvalidJsonObject() throws EnrichmentException {
    var json = new JSONObject();
    json.put(ATTRIBUTE_TO_ENRICH, createJsonObject(null, "referenceid"));

    this.enrichment.enrich(json);
    var enriched = json.getJSONObject(ATTRIBUTE_TO_ENRICH);
    assertThat(enriched.getString("keyA"), is("Initial value"));
    assertThat(enriched.length(), is(2));
  }

  @Test
  public void testEnrich_NoEnrichAttribute() throws EnrichmentException {
    var json = new JSONObject();
    this.enrichment.enrich(json);
    assertFalse(json.has(ATTRIBUTE_TO_ENRICH));
  }

  @Test
  public void testEnrich_ThrowsDataException() throws EnrichmentException, DataException {
    var json = new JSONObject();
    json.put(ATTRIBUTE_TO_ENRICH, createJsonObject("domain", "throwexception"));

    var ex = assertThrows(EnrichmentException.class, () -> this.enrichment.enrich(json));
    assertThat(ex.getMessage(), is("Cannot enrich object."));
    assertThat(ex.getCause(), instanceOf(DataException.class));
  }

  @Test
  public void testEnrich_NonJsonReference() throws EnrichmentException {
    var json = new JSONObject();
    json.put(ATTRIBUTE_TO_ENRICH, "nonjson");

    var ex = assertThrows(EnrichmentException.class, () -> this.enrichment.enrich(json));
    assertThat(ex.getMessage(), is("Cannot enrich object. Only JSONObject or JSONArray is allowed"));
  }

  private static JSONObject createJsonObject(String domain, String id) {
    var json = new JSONObject();
    json.put("domain", domain);
    json.put("id", id);
    json.put("keyA", "Initial value");
    return json;
  }

  private static void assertResult(JSONObject json) {
    assertThat(json.getString("keyA"), is("Value A"));
    assertThat(json.getString("keyB"), is("Value B"));
    assertThat(json.getString("keyC"), is("Value C"));
  }
}
