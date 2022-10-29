package com.devit.mscore.data.service;

import static com.devit.mscore.ApplicationContext.getContext;
import static com.devit.mscore.util.AttributeConstants.CODE;
import static com.devit.mscore.util.AttributeConstants.CREATED_BY;
import static com.devit.mscore.util.AttributeConstants.CREATED_DATE;
import static com.devit.mscore.util.AttributeConstants.LAST_UPDATED_BY;
import static com.devit.mscore.util.AttributeConstants.LAST_UPDATED_DATE;
import static com.devit.mscore.util.AttributeConstants.getCode;
import static com.devit.mscore.util.AttributeConstants.getId;

import com.devit.mscore.Index;
import com.devit.mscore.Logger;
import com.devit.mscore.Repository;
import com.devit.mscore.Schema;
import com.devit.mscore.Service;
import com.devit.mscore.Synchronizer;
import com.devit.mscore.data.enrichment.EnrichmentsExecutor;
import com.devit.mscore.data.filter.FiltersExecutor;
import com.devit.mscore.data.observer.PostProcessObserver;
import com.devit.mscore.data.validation.ValidationsExecutor;
import com.devit.mscore.exception.ApplicationException;
import com.devit.mscore.exception.DataException;
import com.devit.mscore.exception.SynchronizationException;
import com.devit.mscore.exception.ValidationException;
import com.devit.mscore.logging.ApplicationLogger;
import com.devit.mscore.util.DateUtils;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Root class of service implementation. This class provide a generic method of
 * CRUD operation.
 *
 * @param <T> managed class.
 *
 * @author dkakunsi
 */
public class DefaultService implements Service, Synchronizer {

  private static final Logger LOG = ApplicationLogger.getLogger(DefaultService.class);

  private static final String SYNCHRONIZATION_ERROR = "Synchronization failed";

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
    this.index = index;
    this.repository = repository;
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
  public String save(final JSONObject json) throws ApplicationException {
    if (json == null || json.isEmpty()) {
      LOG.warn("Cannot save empty data");
      throw new ValidationException("Cannot save empty data");
    }

    LOG.debug("Saving data {} to database", getCode(json));
    setAuditAttribute(json);
    this.validator.execute(json);

    var result = this.repository.save(json);

    try {
      this.enricher.execute(json);
      this.filter.execute(json);
      this.observers.forEach(o -> new Thread(executeObserver(o, result)).start());
      return getId(result);
    } catch (JSONException e) {
      throw new ApplicationException(e);
    }
  }

  private Runnable executeObserver(PostProcessObserver o, JSONObject r) {
    return () -> {
      try {
        o.notify(r);
      } catch (Exception ex) {
        LOG.error("Error when running observer {}", o.getClass());
      }
    };
  }

  private void setAuditAttribute(JSONObject json) throws DataException {
    var context = getContext();
    try {
      var id = getId(json);
      if (StringUtils.isBlank(id) || !this.repository.find(id).isPresent()) {
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
  public void delete(String id) throws ApplicationException {
    if (StringUtils.isEmpty(id)) {
      throw new ValidationException("Id cannot be empty");
    }
    LOG.debug("Deleting data: {}", id);
    this.repository.delete(id);
  }

  @Override
  public JSONObject find(String id) throws ApplicationException {
    if (StringUtils.isEmpty(id)) {
      throw new ValidationException("Id cannot be empty");
    }

    var optional = this.repository.find(id);
    if (optional.isEmpty()) {
      return new JSONObject();
    }
    var json = optional.get();
    this.filter.execute(json);

    LOG.debug("Found data for id {}: {}", id, json);
    return json;
  }

  @Override
  public JSONArray find(List<String> ids) throws ApplicationException {
    if (ids == null || ids.isEmpty()) {
      throw new ValidationException("Keys cannot be empty");
    }

    var optional = this.repository.find(ids);
    if (optional.isEmpty()) {
      return new JSONArray();
    }

    try {
      var jsons = optional.get();
      this.filter.execute(jsons);
      LOG.debug("Found data for ids {}: {}", ids, jsons);

      return jsons;
    } catch (JSONException e) {
      throw new DataException(e);
    }
  }

  @Override
  public JSONObject findByCode(String code) throws ApplicationException {
    if (StringUtils.isEmpty(code)) {
      throw new ValidationException("Code cannot be empty");
    }

    try {
      var array = this.repository.find(CODE, code);
      if (array.isEmpty()) {
        return new JSONObject();
      }
      var json = array.get().getJSONObject(0);
      this.filter.execute(json);

      LOG.debug("Found data for code {}: {}", code, json);
      return json;
    } catch (JSONException e) {
      throw new DataException(e);
    }
  }

  @Override
  public JSONArray search(JSONObject query) throws ApplicationException {
    return this.index.search(query).orElse(new JSONArray());
  }

  // Synchronizer implementation

  @Override
  public void synchronize() throws SynchronizationException {
    // Just sync the parent, then system will propagate to all children.
    // Parent object is object without parent attribute,
    // which means it is children of no-one.
    synchronize("parent", null);
  }

  @Override
  public void synchronize(String id) throws SynchronizationException {
    try {
      var json = this.repository.find(id);
      if (json.isEmpty()) {
        LOG.info("Cannot synchronize. No data to synchronize in domain {}", getDomain());
        return;
      }
      synchronize(json.get());
    } catch (DataException ex) {
      throw new SynchronizationException(SYNCHRONIZATION_ERROR, ex);
    }
  }

  @Override
  public void synchronize(String searchAttribute, String value)
      throws SynchronizationException {
    try {
      var jsons = this.repository.find(searchAttribute, value);
      if (jsons.isEmpty()) {
        LOG.info("Cannot synchronize. No data to synchronize in domain {}", getDomain());
        return;
      }
      for (Object object : jsons.get()) {
        synchronize((JSONObject) object);
      }
    } catch (DataException ex) {
      throw new SynchronizationException(SYNCHRONIZATION_ERROR, ex);
    }
  }

  private boolean synchronize(JSONObject json) {
    this.enricher.execute(json);
    try {
      var result = this.repository.save(json);
      this.filter.execute(result);
      this.observers.forEach(observer -> observer.notify(result));
      return true;
    } catch (DataException ex) {
      return false;
    }
  }
}
