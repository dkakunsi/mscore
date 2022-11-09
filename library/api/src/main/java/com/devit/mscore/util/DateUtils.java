package com.devit.mscore.util;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DateUtils {

  private static ZoneId zoneId;

  private DateUtils() {
  }

  public static void setZoneId(ZoneId zoneID) {
    zoneId = zoneID;
  }

  public static void setZoneId(String zoneName) {
    zoneId = ZoneId.of(zoneName);
  }

  public static ZoneId getZoneId() {
    if (zoneId == null) {
      return ZoneId.systemDefault();
    }
    return zoneId;
  }

  private static DateTimeFormatter getDateTimeFormatter() {
    return DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(getZoneId());
  }

  public static ZonedDateTime toZonedDateTime(Date date) {
    if (date == null) {
      return null;
    }
    return Instant.ofEpochMilli(date.getTime()).atZone(getZoneId());
  }

  public static ZonedDateTime toZonedDateTime(String dateString) {
    return ZonedDateTime.parse(dateString).withZoneSameInstant(zoneId);
  }

  public static String format(Instant instant) {
    return getDateTimeFormatter().format(instant);
  }

  public static String format(ZonedDateTime zonedDateTime) {
    return format(zonedDateTime.toInstant());
  }

  public static ZonedDateTime now() {
    return ZonedDateTime.now(getZoneId());
  }

  public static String nowString() {
    return format(now());
  }
}
