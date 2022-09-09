package com.devit.mscore;

import org.json.JSONObject;

/**
 * Base interface for workflow proxy object.
 *
 * @author dkakunsi
 */
public abstract class WorkflowObject {

  protected static final String OWNER = "owner";

  protected static final String ORGANISATION = "organisation";

  protected static final String STATUS_CONSTANT = "status";

  protected static final String ACTIVATED = "Active";

  protected static final String COMPLETED = "Complete";

  protected String status;

  public void complete() {
    this.status = COMPLETED;
  }

  public abstract JSONObject toJson();

  public abstract String getId();

  public abstract String getName();
}
