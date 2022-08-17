package com.devit.mscore.web;

public class WebUtils {

  public static final String INFORMATION = "INFORMATION";

  public static final String SUCCESS = "SUCCESS";

  public static final String REDIRECTION = "REDIRECTION";

  public static final String REQUEST_ERROR = "REQUEST ERROR";

  public static final String SERVER_ERROR = "SERVER ERROR";

  public static final String ERROR = "ERROR";

  private WebUtils() {
  }

  public static String getMessageType(int statusCode) {
    if (inRange(statusCode, 100, 200)) {
      return INFORMATION;
    } else if (inRange(statusCode, 200, 300)) {
      return SUCCESS;
    } else if (inRange(statusCode, 300, 400)) {
      return REDIRECTION;
    } else if (inRange(statusCode, 400, 500)) {
      return REQUEST_ERROR;
    } else if (inRange(statusCode, 500, 600)) {
      return SERVER_ERROR;
    } else {
      return ERROR;
    }
  }

  private static boolean inRange(int value, int lower, int upper) {
    return value >= lower && value < upper;
  }
}
