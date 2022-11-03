package com.devit.mscore;

import static org.mockito.Mockito.mock;

import com.devit.mscore.exception.SynchronizationException;
import com.devit.mscore.logging.ApplicationLogger;

import java.util.List;
import java.util.Map;

import org.json.JSONObject;

class SynchronizationTestImpl extends Synchronization {

  private Map<String, JSONObject> map;

  public SynchronizationTestImpl(String referenceDomain, String referenceAttribute, Map<String, JSONObject> map) {
    super(mock(ApplicationLogger.class), referenceDomain, referenceAttribute);
    this.map = map;
  }

  @Override
  protected List<JSONObject> loadFromDatastore(String searchAttribute, String searchValue)
      throws SynchronizationException {
    var result = map.get(searchAttribute);
    return result != null ? List.of(result) : List.of();
  }

  @Override
  protected String getDomain() {
    return "domain";
  }

  @Override
  public Resource getSchema() {
    return null;
  }
}