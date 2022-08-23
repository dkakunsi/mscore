package com.devit.mscore.data.observer;

import org.json.JSONObject;

public interface PostProcessObserver {

  void notify(JSONObject result);

}
