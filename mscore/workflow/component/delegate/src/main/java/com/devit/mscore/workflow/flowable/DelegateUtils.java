package com.devit.mscore.workflow.flowable;

import com.devit.mscore.Configuration;
import com.devit.mscore.Publisher;

import java.util.HashMap;
import java.util.Map;

public class DelegateUtils {

  public static final String NOTIFICATION = "notification";

  private static Configuration configuration;

  private static DataClient dataClient;

  private static Map<String, Publisher> publisherMap;

  static Configuration getConfiguration() {
    return configuration;
  }

  private DelegateUtils() {
  }

  public static void setConfiguration(Configuration newConfiguration) {
    configuration = newConfiguration;
  }

  static Publisher getPublisher(String target) {
    return publisherMap.get(target);
  }

  public static void setPublishers(Map<String, Publisher> publishers) {
    publisherMap = new HashMap<>(publishers);
  }

  static DataClient getDataClient() {
    return dataClient;
  }

  public static void setDataClient(DataClient newDataClient) {
    dataClient = newDataClient;
  }
}
