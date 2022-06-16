package com.devit.mscore.workflow.flowable;

import java.util.Map;

import com.devit.mscore.Configuration;
import com.devit.mscore.Publisher;

public class DelegateUtils {

    public static final String NOTIFICATION = "notification";

    private static Configuration systemConfig;

    private static DataClient dataClient;

    private static Map<String, Publisher> publisherMap;

    static Configuration getConfiguration() {
        return systemConfig;
    }

    private DelegateUtils() {
    }

    public static void setConfiguration(Configuration configuration) {
        systemConfig = configuration;
    }

    static Publisher getPublisher(String target) {
        return publisherMap.get(target);
    }

    public static void setPublishers(Map<String, Publisher> publishers) {
        publisherMap = publishers;
    }

    static DataClient getDataClient() {
        return dataClient;
    }

    public static void setDataClient(DataClient newDataClient) {
        dataClient = newDataClient;
    }
}
