package com.devit.mscore.service;

import static com.devit.mscore.util.AttributeConstants.CODE;
import static com.devit.mscore.util.AttributeConstants.CREATED_BY;
import static com.devit.mscore.util.AttributeConstants.CREATED_DATE;
import static com.devit.mscore.util.AttributeConstants.LAST_UPDATED_BY;
import static com.devit.mscore.util.AttributeConstants.LAST_UPDATED_DATE;
import static com.devit.mscore.util.AttributeConstants.getCode;
import static com.devit.mscore.util.AttributeConstants.getId;

import java.util.ArrayList;
import java.util.List;

import com.devit.mscore.ApplicationContext;
import com.devit.mscore.Service;
import com.devit.mscore.Index;
import com.devit.mscore.Repository;
import com.devit.mscore.Schema;
import com.devit.mscore.Synchronizer;
import com.devit.mscore.enrichment.EnrichmentsExecutor;
import com.devit.mscore.filter.FiltersExecutor;
import com.devit.mscore.observer.PostProcessObserver;
import com.devit.mscore.validation.ValidationsExecutor;
import com.devit.mscore.exception.ApplicationException;
import com.devit.mscore.exception.DataException;
import com.devit.mscore.exception.SynchronizationException;
import com.devit.mscore.exception.ValidationException;
import com.devit.mscore.util.DateUtils;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Root class of service implementation. This class provide a generic method of
 * CRUD operation.
 * 
 * @param <T> managed class.
 * 
 * @author dkakunsi
 */
public class DefaultService implements Service, Synchronizer {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultService.class);

    private static final String SYNCHRONIZATION_ERROR = "Synchronization failed.";

    protected Schema schema;

    protected Repository repository;

    protected Index index;

    protected ValidationsExecutor validator;

    protected FiltersExecutor filter;

    protected EnrichmentsExecutor enricher;

    protected List<PostProcessObserver> observers;

    public DefaultService(Schema schema) {
        this.schema = schema;
        this.observers = new ArrayList<>();
    }

    public DefaultService(Schema schema, Repository repository, Index index, ValidationsExecutor validator,
            FiltersExecutor filter, EnrichmentsExecutor enricher) {
        this(schema);
        this.repository = repository;
        this.index = index;
        this.validator = validator;
        this.enricher = enricher;
        this.filter = filter;
    }

    public DefaultService addObserver(PostProcessObserver observer) {
        this.observers.add(observer);
        return this;
    }

    @Override
    public String getDomain() {
        return this.schema.getDomain();
    }

    @Override
    public Schema getSchema() {
        return this.schema;
    }

    @Override
    public String save(ApplicationContext context, final JSONObject json) throws ApplicationException {
        if (json == null || json.isEmpty()) {
            LOG.warn("BreadcrumbId: {}. Cannot save empty data.", context.getBreadcrumbId());
            throw new ValidationException("Cannot save empty data.");
        }

        LOG.debug("BreadcrumbId: {}. Saving data {} to database.", context.getBreadcrumbId(), getCode(json));
        setAuditAttribute(context, json);
        this.validator.execute(context, json);
        this.enricher.execute(context, json);

        var result = this.repository.save(context, json);

        try {
            this.filter.execute(context, json);
            this.observers.forEach(observer -> observer.notify(context, result));
            return getId(result);
        } catch (JSONException e) {
            throw new ApplicationException(e);
        }
    }

    private void setAuditAttribute(ApplicationContext context, JSONObject json) throws DataException {
        try {
            var id = getId(json);
            if (StringUtils.isBlank(id) || !this.repository.find(context, id).isPresent()) {
                json.put(CREATED_DATE, DateUtils.nowString());
                json.put(CREATED_BY, context.getRequestedBy());
            }
            json.put(LAST_UPDATED_DATE, DateUtils.nowString());
            json.put(LAST_UPDATED_BY, context.getRequestedBy());
        } catch (JSONException e) {
            throw new DataException(e);
        }
    }

    @Override
    public void delete(ApplicationContext context, String id) throws ApplicationException {
        if (StringUtils.isEmpty(id)) {
            throw new ValidationException("Id cannot be empty.");
        }
        LOG.debug("BreadcrumbId: {}. Deleting data: {}.", context.getBreadcrumbId(), id);
        this.repository.delete(context, id);
    }

    @Override
    public JSONObject find(ApplicationContext context, String id) throws ApplicationException {
        if (StringUtils.isEmpty(id)) {
            throw new ValidationException("Id cannot be empty.");
        }

        var optional = this.repository.find(context, id);
        if (optional.isEmpty()) {
            return new JSONObject();
        }
        var json = optional.get();
        this.filter.execute(context, json);

        LOG.debug("BreadcrumbId: {}. Found data for id {}: {}.", context.getBreadcrumbId(), id, json);
        return json;
    }

    @Override
    public JSONArray find(ApplicationContext context, List<String> ids) throws ApplicationException {
        if (ids == null || ids.isEmpty()) {
            throw new ValidationException("Keys cannot be empty.");
        }

        var optional = this.repository.find(context, ids);
        if (optional.isEmpty()) {
            return new JSONArray();
        }

        try {
            var jsons = optional.get();
            this.filter.execute(context, jsons);
            LOG.debug("BreadcrumbId: {}. Found data for ids {}: {}.", context.getBreadcrumbId(), ids, jsons);

            return jsons;
        } catch (JSONException e) {
            throw new DataException(e);
        }
    }

    @Override
    public JSONObject findByCode(ApplicationContext context, String code) throws ApplicationException {
        if (StringUtils.isEmpty(code)) {
            throw new ValidationException("Code cannot be empty.");
        }

        try {
            var array = this.repository.find(context, CODE, code);
            if (array.isEmpty()) {
                return new JSONObject();
            }
            var json = array.get().getJSONObject(0);
            this.filter.execute(context, json);

            LOG.debug("BreadcrumbId: {}. Found data for code {}: {}.", context.getBreadcrumbId(), code, json);
            return json;
        } catch (JSONException e) {
            throw new DataException(e);
        }
    }

    @Override
    public JSONArray search(ApplicationContext context, JSONObject query) throws ApplicationException {
        return this.index.search(context, query).orElse(new JSONArray());
    }

    // Synchronizer implementation

    @Override
    public void synchronize(ApplicationContext context) throws SynchronizationException {
        // Just sync the parent, then system will propagate to all children.
        // Parent object is object without parent attribute,
        // which means it is children of no-one.
        synchronize(context, "parent", null);
    }

    @Override
    public void synchronize(ApplicationContext context, String id) throws SynchronizationException {
        try {
            var json = this.repository.find(context, id);
            if (json.isEmpty()) {
                LOG.info("BreadcrumbId: {}. Cannot synchronize. No data to synchronize in domain {}.",
                        context.getBreadcrumbId(), getDomain());
                return;
            }
            synchronize(context, json.get());
        } catch (DataException ex) {
            throw new SynchronizationException(SYNCHRONIZATION_ERROR, ex);
        }
    }

    @Override
    public void synchronize(ApplicationContext context, String searchAttribute, String value)
            throws SynchronizationException {
        try {
            var jsons = this.repository.find(context, searchAttribute, value);
            if (jsons.isEmpty()) {
                LOG.info("BreadcrumbId: {}. Cannot synchronize. No data to synchronize in domain {}.",
                        context.getBreadcrumbId(), getDomain());
                return;
            }
            for (Object object : jsons.get()) {
                synchronize(context, (JSONObject) object);
            }
        } catch (DataException ex) {
            throw new SynchronizationException(SYNCHRONIZATION_ERROR, ex);
        }
    }

    private boolean synchronize(ApplicationContext context, JSONObject json) {
        this.enricher.execute(context, json);
        try {
            var result = this.repository.save(context, json);
            this.filter.execute(context, result);
            this.observers.forEach(observer -> observer.notify(context, result));
            return true;
        } catch (DataException ex) {
            return false;
        }
    }
}
