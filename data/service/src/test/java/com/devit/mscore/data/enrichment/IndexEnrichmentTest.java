package com.devit.mscore.data.enrichment;

import static org.junit.Assert.assertTrue;

import com.devit.mscore.exception.DataException;

import java.util.HashMap;

import org.junit.Test;

public class IndexEnrichmentTest {
  
  @Test
  public void testLoadData_WithNullIndex() throws DataException {
    var enrichment = new IndexEnrichment(new HashMap<>(), "domain", "attribute");
    var result = enrichment.loadFromDataStore("domain", "id");
    
    assertTrue(result.isEmpty());
  }
}
