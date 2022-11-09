package com.devit.mscore.gateway.service;

import static com.devit.mscore.util.Constants.ID;
import static com.devit.mscore.util.Constants.TASK;

import com.devit.mscore.Event;
import com.devit.mscore.ServiceRegistration;
import com.devit.mscore.exception.WebClientException;
import com.devit.mscore.web.Client;

import java.util.Optional;

import org.json.JSONObject;

public class TaskService extends AbstractGatewayService {

  private static final String TASK_ACTION = "task.update";

  protected TaskService(ServiceRegistration serviceRegistration, Client client) {
    super(serviceRegistration, client);
  }

  @Override
  public String getDomain() {
    return "task";
  }

  public String completeTask(String taskId, JSONObject taskResponse) throws WebClientException {
    var entity = new JSONObject();
    entity.put(ID, taskId);

    var uri = getUri(TASK);
    var event = Event.of(Event.Type.UPDATE, TASK, TASK_ACTION, entity, taskResponse);
    this.client.post(uri, Optional.of(event.toJson()));

    return taskId;
  }
}
