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

  protected final Long syncDelay;

  protected Index index;

  protected EnrichmentsExecutor enricher;

  protected SynchronizationObserver syncObserver;

  public IndexingObserver(Index index, EnrichmentsExecutor enricher, SynchronizationObserver syncObserver, Long syncDelay) {
    this.index = index;
    this.enricher = enricher;
    this.syncObserver = syncObserver;
    this.syncDelay = syncDelay;
  }

  @Override
  public void notify(JSONObject json) {
    if (index == null) {
      LOG.warn("Index is not provided. By pass indexing");
      return;
    }

    LOG.info("Indexing document '{}' into index '{}'", getId(json), getDomain(json));
    try {
      var dataToIndex = new JSONObject(json.toString());
      enricher.execute(dataToIndex);
      index.index(dataToIndex);
      Thread.sleep(syncDelay);
      syncObserver.notify(json);
    } catch (IndexingException | InterruptedException ex) {
      LOG.error(INDEXING_ERROR, ex);
    }
  }
}
