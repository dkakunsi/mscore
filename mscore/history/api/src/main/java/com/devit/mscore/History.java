package com.devit.mscore;

import org.json.JSONObject;

public interface History {

    void create(ApplicationContext context, JSONObject message) throws HistoryException;
    
}
