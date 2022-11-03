package com.devit.mscore;

import org.json.JSONObject;

public interface PostProcessObserver {

  void notify(JSONObject result);

}
