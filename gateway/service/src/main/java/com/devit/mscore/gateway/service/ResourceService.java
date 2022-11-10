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
        processByWorkflow(domain, action.get(), Event.Type.CREATE, entity, variables);
      } else {
        // TODO; add validation to only ADMIN can do this
        submitByEvent(domain, Event.Type.CREATE, entity);
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

  public String update(String domain, String id, JSONObject payload) throws WebClientException {
    var context = getContext();
    var entity = payload.getJSONObject(ENTITY);
    entity.put(ID, id);
    var variables = payload.optJSONObject(VARIABLE);
    try {
      var action = context.getAction();
      if (action.isPresent()) {
        processByWorkflow(domain, action.get(), Event.Type.UPDATE, entity, variables);
      } else {
        // TODO; add validation to only ADMIN can do this
        submitByEvent(domain, Event.Type.UPDATE, entity);
      }
      return id;
    } catch (ApplicationRuntimeException ex) {
      throw new WebClientException(WEBCLIENT_EXCEPTION_MESSAGE, ex);
    }
  }

  private JSONObject processByWorkflow(String domain, String action, Event.Type eventType, JSONObject entity,
      JSONObject variable) throws WebClientException {
    var uri = getUri(PROCESS);
    var event = Event.of(eventType, domain, action, entity, variable);
    return client.post(uri, Optional.of(event.toJson()));
  }

  private void submitByEvent(String domain, Event.Type eventType, JSONObject entity) {
    var event = Event.of(eventType, domain, entity);
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
