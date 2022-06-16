package com.devit.mscore.util;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.hamcrest.MatcherAssert.assertThat;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;

import org.junit.Test;

public class DateUtilTest {

    @Test
    public void testToZonedLocalDateTime() {
        // Default ZoneId
        var date = new Date();
        var zonedDateTime = DateUtils.toZonedDateTime(date);
        assertNotNull(zonedDateTime);

        // Null Date
        zonedDateTime = DateUtils.toZonedDateTime((Date) null);
        assertNull(zonedDateTime);

        // Defined ZoneId
        DateUtils.setZoneId(ZoneId.of("Asia/Makassar"));
        date = new Date();
        zonedDateTime = DateUtils.toZonedDateTime(date);
        assertNotNull(zonedDateTime);
        assertThat(zonedDateTime.getZone(), is(ZoneId.of("Asia/Makassar")));

        // To String
        zonedDateTime = ZonedDateTime.of(2021, 01, 01, 01, 01, 01, 0, ZoneId.of("Asia/Makassar"));
        var dateString = DateUtils.format(zonedDateTime);
        var expected = "2021-01-01T01:01:01+08:00";
        assertThat(dateString, is(expected));

        // From String
        zonedDateTime = DateUtils.toZonedDateTime(dateString);
        assertThat(zonedDateTime.getYear(), is(2021));
        assertThat(zonedDateTime.getMonth(), is(Month.JANUARY));
        assertThat(zonedDateTime.getDayOfMonth(), is(1));
        assertThat(zonedDateTime.getHour(), is(1));
        assertThat(zonedDateTime.getMinute(), is(1));
        assertThat(zonedDateTime.getSecond(), is(1));
        assertThat(zonedDateTime.getOffset(), is(ZoneOffset.of("+08:00")));
        assertThat(zonedDateTime.getZone(), is(ZoneId.of("Asia/Makassar")));
    }

    @Test
    public void testFormat() {
        DateUtils.setZoneId("Asia/Makassar");

        var localDateTime = LocalDateTime.of(2021, 1, 1, 1, 1, 1);
        var actual = DateUtils.format(localDateTime.toInstant(ZoneOffset.of("+08:00")));
        var expected = "2021-01-01T01:01:01+08:00";
        assertThat(actual, is(expected));

        var zonedDateTime = ZonedDateTime.of(2021, 1, 1, 1, 1, 1, 0, ZoneId.of("Asia/Makassar"));
        actual = DateUtils.format(zonedDateTime.toInstant());
        expected = "2021-01-01T01:01:01+08:00";
        assertThat(actual, is(expected));
    }

    @Test
    public void testCreateDate() {
        DateUtils.setZoneId("Asia/Makassar");

        var now = DateUtils.now();
        assertThat(now.getOffset().toString(), is("+08:00"));

        var nowString = DateUtils.nowString();
        now = DateUtils.toZonedDateTime(nowString);
        assertThat(now.getOffset().toString(), is("+08:00"));
    }
}
