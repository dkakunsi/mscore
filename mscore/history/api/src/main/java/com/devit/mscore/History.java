package com.devit.mscore;

import org.json.JSONObject;

public interface History {

  void create(JSONObject message) throws HistoryException;

}
