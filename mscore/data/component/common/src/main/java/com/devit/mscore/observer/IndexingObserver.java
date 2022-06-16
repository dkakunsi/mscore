package com.devit.mscore.observer;

import com.devit.mscore.ApplicationContext;
import com.devit.mscore.Index;
import com.devit.mscore.exception.IndexingException;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IndexingObserver implements PostProcessObserver {

    private static final Logger LOG = LoggerFactory.getLogger(IndexingObserver.class);

    private static final String INDEXING_ERROR = "BreadcrumbId: {}. Indexing document failed.";

    protected Index index;

    public IndexingObserver(Index index) {
        this.index = index;
    }

    @Override
    public void notify(ApplicationContext context, JSONObject json) {
        if (this.index == null) {
            LOG.warn("BreadcrumbId: {}. Index is not provided. By pass indexing.", context.getBreadcrumbId());
            return;
        }

        try {
            LOG.debug("BreadcrumbId: {}. Indexing document: {}.", context.getBreadcrumbId(), json);
            this.index.index(context, json);
        } catch (IndexingException ex) {
            LOG.error(INDEXING_ERROR, context.getBreadcrumbId(), ex);
        }
    }
}
