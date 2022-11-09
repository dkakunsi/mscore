package com.devit.mscore.gateway.service;

import static com.devit.mscore.ApplicationContext.getContext;
import static com.devit.mscore.util.AttributeConstants.getId;
import static com.devit.mscore.util.AttributeConstants.hasId;
import static com.devit.mscore.util.Constants.ENTITY;
import static com.devit.mscore.util.Constants.ID;
import static com.devit.mscore.util.Constants.PROCESS;
import static com.devit.mscore.util.Constants.VARIABLE;

import com.devit.mscore.Event;
import com.devit.mscore.Publisher;
import com.devit.mscore.ServiceRegistration;
import com.devit.mscore.exception.ApplicationRuntimeException;
import com.devit.mscore.exception.WebClientException;
import com.devit.mscore.web.Client;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.json.JSONObject;

public class ResourceService extends AbstractGatewayService {

  private Publisher publisher;

  private String domainEventChannel;

  public ResourceService(ServiceRegistration serviceRegistration, Client client, Publisher publisher,
      String domainEventChannel) {
    super(serviceRegistration, client);
    this.publisher = publisher;
    this.domainEventChannel = domainEventChannel;
  }

  @Override
  public String getDomain() {
    return "resource";
  }

  public String create(String domain, JSONObject payload) throws WebClientException {
    var context = getContext();
    var entity = payload.getJSONObject(ENTITY);
    var variables = payload.optJSONObject(VARIABLE);
    var id = getOrCreateId(entity);
    try {
      var action = context.getAction();
      if (action.isPresent()) {
        createByWorkflow(domain, action.get(), entity, variables);
      } else {
        // TODO; add validation to only ADMIN can do this
        createByEvent(domain, entity);
      }
      return id;
    } catch (ApplicationRuntimeException ex) {
      throw new WebClientException(WEBCLIENT_EXCEPTION_MESSAGE, ex);
    }
  }

  private static String getOrCreateId(JSONObject json) {
    if (!hasId(json)) {
      var id = UUID.randomUUID().toString();
      json.put(ID, id);
    }
    return getId(json);
  }

  private JSONObject createByWorkflow(String domain, String action, JSONObject entity, JSONObject variable)
      throws WebClientException {
    var uri = getUri(PROCESS);
    var event = Event.of(Event.Type.CREATE, domain, action, entity, variable);
    return client.post(uri, Optional.of(event.toJson()));
  }

  private void createByEvent(String domain, JSONObject entity) {
    var event = Event.of(Event.Type.CREATE, domain, entity);
    publisher.publish(domainEventChannel, event.toJson());
  }

  public String update(String domain, String id, JSONObject payload) throws WebClientException {
    var context = getContext();
    var entity = payload.getJSONObject(ENTITY);
    entity.put(ID, id);
    var variables = payload.optJSONObject(VARIABLE);
    try {
      var action = context.getAction();
      if (action.isPresent()) {
        updateByWorkflow(domain, action.get(), id, entity, variables);
      } else {
        // TODO; add validation to only ADMIN can do this
        updateByEvent(domain, id, entity);
      }
      return id;
    } catch (ApplicationRuntimeException ex) {
      throw new WebClientException(WEBCLIENT_EXCEPTION_MESSAGE, ex);
    }
  }

  private JSONObject updateByWorkflow(String domain, String action, String id, JSONObject entity, JSONObject variables)
      throws WebClientException {
    var uri = String.format("%s/%s", getUri(domain), id);
    var event = Event.of(Event.Type.UPDATE, domain, action, entity, variables);
    return client.put(uri, Optional.of(event.toJson()));
  }

  private void updateByEvent(String domain, String id, JSONObject entity) {
    var event = Event.of(Event.Type.UPDATE, domain, entity);
    publisher.publish(domainEventChannel, event.toJson());
  }

  public JSONObject getById(String domain, String id) throws WebClientException {
    try {
      var uri = String.format("%s/%s", getUri(domain), id);
      return client.get(uri, Map.of());
    } catch (ApplicationRuntimeException ex) {
      throw new WebClientException(WEBCLIENT_EXCEPTION_MESSAGE, ex);
    }
  }

  public JSONObject getByCode(String domain, String code) throws WebClientException {
    try {
      var uri = String.format("%s/code/%s", getUri(domain), code);
      return client.get(uri, null);
    } catch (ApplicationRuntimeException ex) {
      throw new WebClientException(WEBCLIENT_EXCEPTION_MESSAGE, ex);
    }
  }

  public JSONObject getMany(String domain, String ids) throws WebClientException {
    try {
      var uri = String.format("%s/keys", getUri(domain));
      return client.get(uri, Map.of("ids", ids));
    } catch (ApplicationRuntimeException ex) {
      throw new WebClientException(WEBCLIENT_EXCEPTION_MESSAGE, ex);
    }
  }

  public JSONObject sync(String domain) throws WebClientException {
    try {
      var uri = String.format("%s/sync", getUri(domain));
      return client.post(uri, Optional.empty());
    } catch (ApplicationRuntimeException ex) {
      throw new WebClientException(WEBCLIENT_EXCEPTION_MESSAGE, ex);
    }
  }

  public JSONObject syncById(String domain, String id) throws WebClientException {
    try {
      var uri = String.format("%s/%s/sync", getUri(domain), id);
      return client.post(uri, Optional.empty());
    } catch (ApplicationRuntimeException ex) {
      throw new WebClientException(WEBCLIENT_EXCEPTION_MESSAGE, ex);
    }
  }

  public JSONObject search(String domain, JSONObject criteria) throws WebClientException {
    try {
      var uri = String.format("%s/search", getUri(domain));
      return client.post(uri, Optional.of(criteria));
    } catch (ApplicationRuntimeException ex) {
      throw new WebClientException(WEBCLIENT_EXCEPTION_MESSAGE, ex);
    }
  }
}
