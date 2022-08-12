package com.devit.mscore.observer;

import org.json.JSONObject;

public interface PostProcessObserver {

    void notify(JSONObject result);

}
