package com.devit.mscore.data.observer;

import static com.devit.mscore.util.AttributeConstants.getDomain;
import static com.devit.mscore.util.AttributeConstants.getId;

import com.devit.mscore.Index;
import com.devit.mscore.Logger;
import com.devit.mscore.PostProcessObserver;
import com.devit.mscore.data.enrichment.EnrichmentsExecutor;
import com.devit.mscore.exception.IndexingException;
import com.devit.mscore.logging.ApplicationLogger;

import org.json.JSONObject;

public class IndexingObserver implements PostProcessObserver {

  private static final Logger LOG = ApplicationLogger.getLogger(IndexingObserver.class);

  private static final String INDEXING_ERROR = "Indexing document failed";

  protected Index index;

  protected EnrichmentsExecutor enricher;

  public IndexingObserver(Index index, EnrichmentsExecutor enricher) {
    this.index = index;
    this.enricher = enricher;

  }

  @Override
  public void notify(JSONObject json) {
    if (this.index == null) {
      LOG.warn("Index is not provided. By pass indexing");
      return;
    }

    LOG.info("Indexing document '{}' into index '{}'", getId(json), getDomain(json));
    try {
      var dataToIndex = new JSONObject(json.toString());
      this.enricher.execute(dataToIndex);
      this.index.index(dataToIndex);
    } catch (IndexingException ex) {
      LOG.error(INDEXING_ERROR, ex);
    }
  }
}
