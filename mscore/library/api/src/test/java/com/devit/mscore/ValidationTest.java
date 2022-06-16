package com.devit.mscore;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.json.JSONObject;
import org.junit.Test;

public class ValidationTest {

    @Test
    public void testGetDomain() {
        var validation = new Validation() {
            @Override
            public boolean validate(ApplicationContext context, JSONObject json) {
                return false;
            }
        };

        assertThat(validation.getDomain(), is("all"));
    }
}
