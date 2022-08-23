package com.devit.mscore.data.enrichment;

import com.devit.mscore.Enrichment;

import java.util.Optional;

import org.json.JSONObject;

/**
 * <p>
 * No operation enrichment.
 * </p>
 * <p>
 * This class is intended for an already complete object, but need attribute
 * enrichment.
 * </p>
 */
public class NoopEnrichment extends Enrichment {

  public NoopEnrichment(String domain, String attribute) {
    super(domain, attribute);
  }

  @Override
  protected boolean isValid(JSONObject object) {
    return true;
  }

  @Override
  protected Optional<JSONObject> loadFromDataStore(String domain, String id) {
    return Optional.empty();
  }
}
