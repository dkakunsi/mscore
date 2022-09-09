package com.devit.mscore;

import static com.devit.mscore.util.DateUtils.toZonedDateTime;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

public class WorkflowInstanceTest {

  private Date dueDate = new Date();

  @Test
  public void testToJson() {
    var task = new DummyTask();
    var instance = new DummyInstance();
    instance.complete();
    var actual = instance.toJson(List.of(task));
    var expected = getExpectedInstanceJson();

    assertTrue(expected.similar(actual));
  }

  private JSONObject getExpectedTaskJson() {
    var expected = new JSONObject();
    expected.put("id", "id");
    expected.put("name", "name");
    expected.put("dueDate", toZonedDateTime(this.dueDate));
    expected.put("assignee", "assignee");
    expected.put("organisation", "organisation");
    expected.put("executionId", "executionId");
    expected.put("owner", "owner");
    return expected;
  }

  private JSONObject getExpectedInstanceJson() {
    var expected = new JSONObject();
    expected.put("owner", "owner");
    expected.put("organisation", "organisation");
    expected.put("createdBy", "startUserId");
    expected.put("domain", "workflow");
    expected.put("id", "processInstanceId");
    expected.put("action", "action");
    expected.put("name", "name");
    expected.put("status", "Complete");

    var entity = new JSONObject();
    entity.put("id", "businessKey");
    entity.put("domain", "domain");
    expected.put("entity", entity);

    var tasks = new JSONArray();
    tasks.put(getExpectedTaskJson());
    expected.put("task", tasks);
    return expected;
  }

  class DummyInstance extends WorkflowInstance {

    @Override
    protected String getStartUserId() {
      return "startUserId";
    }

    @Override
    protected String getBusinessKey() {
      return "businessKey";
    }

    @Override
    protected String getOwner() {
      return "owner";
    }

    @Override
    protected String getOrganisation() {
      return "organisation";
    }

    @Override
    protected String getDomain() {
      return "domain";
    }

    @Override
    protected String getAction() {
      return "action";
    }

    @Override
    protected String getProcessInstanceId() {
      return "processInstanceId";
    }

    @Override
    public String getId() {
      return "id";
    }

    @Override
    public String getName() {
      return "name";
    }
  }

  class DummyTask extends WorkflowTask {

    @Override
    protected String getOwner() {
      return "owner";
    }

    @Override
    protected String getExecutionId() {
      return "executionId";
    }

    @Override
    protected String getOrganisation() {
      return "organisation";
    }

    @Override
    protected String getAssignee() {
      return "assignee";
    }

    @Override
    protected Date getDueDate() {
      return dueDate;
    }

    @Override
    public String getInstanceId() {
      return "instanceId";
    }

    @Override
    public String getId() {
      return "id";
    }

    @Override
    public String getName() {
      return "name";
    }
  }
}
