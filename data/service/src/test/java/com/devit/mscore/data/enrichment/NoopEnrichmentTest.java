package com.devit.mscore.data.enrichment;

import static org.junit.Assert.assertTrue;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

public class NoopEnrichmentTest {

  private NoopEnrichment enrichment;

  @Before
  public void setup() {
    this.enrichment = new NoopEnrichment("domain", "attribute");
  }

  @Test
  public void testIsValid() {
    assertTrue(this.enrichment.isValid(new JSONObject()));
  }

  @Test
  public void testLoadFromDataStore() {
    var result = this.enrichment.loadFromDataStore("domain", "id");
    assertTrue(result.isEmpty());
  }
}
