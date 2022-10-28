package com.devit.mscore.data.observer;

import static com.devit.mscore.util.AttributeConstants.getId;

import com.devit.mscore.Index;
import com.devit.mscore.Logger;
import com.devit.mscore.exception.IndexingException;
import com.devit.mscore.logging.ApplicationLogger;

import org.json.JSONObject;

public class IndexingObserver implements PostProcessObserver {

  private static final Logger LOG = ApplicationLogger.getLogger(IndexingObserver.class);

  private static final String INDEXING_ERROR = "Indexing document failed";

  protected Index index;

  public IndexingObserver(Index index) {
    this.index = index;
  }

  @Override
  public void notify(JSONObject json) {
    if (this.index == null) {
      LOG.warn("Index is not provided. By pass indexing");
      return;
    }

    try {
      LOG.info("Indexing document: {}", getId(json));
      this.index.index(json);
    } catch (IndexingException ex) {
      LOG.error(INDEXING_ERROR, ex);
    }
  }
}
