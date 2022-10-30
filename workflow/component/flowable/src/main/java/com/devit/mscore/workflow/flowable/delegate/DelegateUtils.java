package com.devit.mscore.workflow.flowable.delegate;

import com.devit.mscore.Configuration;
import com.devit.mscore.DataClient;
import com.devit.mscore.Publisher;

import java.util.Map;

public class DelegateUtils {

  public static final String NOTIFICATION = "notification";

  private static Configuration configuration;

  private static DataClient dataClient;

  private static Map<String, String> channels;

  private static Publisher publisher;

  static Configuration getConfiguration() {
    return configuration;
  }

  private DelegateUtils() {
  }

  public static void setConfiguration(Configuration newConfiguration) {
    configuration = newConfiguration;
  }

  public static Publisher getPublisher() {
    return publisher;
  }

  public static void setPublisher(Publisher p) {
    publisher = p;
  }

  public static DataClient getDataClient() {
    return dataClient;
  }

  public static void setDataClient(DataClient dc) {
    dataClient = dc;
  }

  public static String getChannel(String target) {
    return channels.get(target);
  }

  public static void setChannels(Map<String, String> m) {
    channels = m;
  }
}
