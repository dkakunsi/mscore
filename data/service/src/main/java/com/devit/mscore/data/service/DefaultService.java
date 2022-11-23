package com.devit.mscore.data.service;

import static com.devit.mscore.ApplicationContext.getContext;
import static com.devit.mscore.util.AttributeConstants.CODE;
import static com.devit.mscore.util.AttributeConstants.CREATED_BY;
import static com.devit.mscore.util.AttributeConstants.CREATED_DATE;
import static com.devit.mscore.util.AttributeConstants.LAST_UPDATED_BY;
import static com.devit.mscore.util.AttributeConstants.LAST_UPDATED_DATE;
import static com.devit.mscore.util.AttributeConstants.getCode;
import static com.devit.mscore.util.AttributeConstants.getId;

import com.devit.mscore.FiltersExecutor;
import com.devit.mscore.Index;
import com.devit.mscore.Index.SearchCriteria;
import com.devit.mscore.Logger;
import com.devit.mscore.PostProcessObserver;
import com.devit.mscore.Repository;
import com.devit.mscore.Resource;
import com.devit.mscore.Schema;
import com.devit.mscore.Service;
import com.devit.mscore.data.validation.ValidationsExecutor;
import com.devit.mscore.exception.ApplicationException;
import com.devit.mscore.exception.DataException;
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
public class DefaultService implements Service {

  private static final Logger LOG = ApplicationLogger.getLogger(DefaultService.class);

  protected Schema schema;

  protected Repository repository;

  protected Index index;

  protected ValidationsExecutor validator;

  protected FiltersExecutor filter;

  protected List<PostProcessObserver> observers;

  public DefaultService(Schema schema) {
    this.schema = schema;
    observers = new ArrayList<>();
  }

  public DefaultService(Schema schema, Repository repository, Index index, ValidationsExecutor validator,
      FiltersExecutor filter) {
    this(schema);
    this.index = index;
    this.repository = repository;
    this.validator = validator;
    this.filter = filter;
  }

  public DefaultService addObserver(PostProcessObserver observer) {
    observers.add(observer);
    return this;
  }

  @Override
  public String getDomain() {
    return schema.getDomain();
  }

  @Override
  public Resource getSchema() {
    return schema;
  }

  @Override
  public String save(final JSONObject json) throws ApplicationException {
    if (json == null || json.isEmpty()) {
      LOG.warn("Cannot save empty data");
      throw new ValidationException("Cannot save empty data");
    }

    LOG.debug("Saving data '{}' to database", getCode(json));
    setAuditAttribute(json);
    validator.execute(json);

    var result = repository.save(json);

    try {
      filter.execute(json);
      observers.forEach(o -> new Thread(executeObserver(o, result)).start());
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
        var domain = com.devit.mscore.util.AttributeConstants.getDomain(r);
        var code = getCode(r);
        LOG.error("Error when running observer '{}' for domain '{}' and code '{}'", ex, o.getClass(), domain, code);
      }
    };
  }

  private void setAuditAttribute(JSONObject json) throws DataException {
    var context = getContext();
    try {
      var id = getId(json);
      if (StringUtils.isBlank(id) || !repository.find(id).isPresent()) {
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
    LOG.debug("Deleting data '{}' from database", id);
    repository.delete(id);
  }

  @Override
  public JSONObject find(String id) throws ApplicationException {
    if (StringUtils.isEmpty(id)) {
      throw new ValidationException("Id cannot be empty");
    }

    var optional = repository.find(id);
    if (optional.isEmpty()) {
      return new JSONObject();
    }
    var json = optional.get();
    filter.execute(json);

    LOG.debug("Found data with id '{}': '{}'", id, json);
    return json;
  }

  @Override
  public JSONArray find(List<String> ids) throws ApplicationException {
    if (ids == null || ids.isEmpty()) {
      throw new ValidationException("Keys cannot be empty");
    }

    var optional = repository.find(ids);
    if (optional.isEmpty()) {
      return new JSONArray();
    }

    try {
      var jsons = optional.get();
      filter.execute(jsons);

      LOG.debug("Found data with ids '{}': '{}'", ids, jsons);
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
      var array = repository.find(CODE, code);
      if (array.isEmpty()) {
        return new JSONObject();
      }
      var json = array.get().getJSONObject(0);
      filter.execute(json);

      LOG.debug("Found data with code '{}': '{}'", code, json);
      return json;
    } catch (JSONException e) {
      throw new DataException(e);
    }
  }

  @Override
  public JSONArray search(JSONObject query) throws ApplicationException {
    var criteria = SearchCriteria.from(query);
    return index.search(criteria).orElse(new JSONArray());
  }
}
