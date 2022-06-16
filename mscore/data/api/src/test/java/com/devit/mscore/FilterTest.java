package com.devit.mscore;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.json.JSONObject;
import org.junit.Test;

public class FilterTest {

    @Test
    public void testFilter() {
        var filter = new Filter(List.of("attribute")) {

            @Override
            protected void apply(JSONObject json, String key) {
            }
        };
        assertThat(filter.getDomain(), is("all"));

        var json = new JSONObject("{\"domain\":\"domain\",\"attribute\":\"attribute\"}");
        filter.filter(json);
        assertTrue(json.has("domain"));
        assertTrue(json.has("attribute"));
    }
}
