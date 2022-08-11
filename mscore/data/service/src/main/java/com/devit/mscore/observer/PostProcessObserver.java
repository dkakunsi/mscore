package com.devit.mscore.observer;

import com.devit.mscore.ApplicationContext;

import org.json.JSONObject;

public interface PostProcessObserver {

    void notify(ApplicationContext context, JSONObject result);

}
