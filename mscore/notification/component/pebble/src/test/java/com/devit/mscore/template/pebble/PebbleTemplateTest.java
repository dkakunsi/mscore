package com.devit.mscore.template.pebble;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThrows;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import com.devit.mscore.exception.TemplateException;

import org.json.JSONObject;
import org.junit.Test;

public class PebbleTemplateTest {
    
    @Test 
    public void testBuild() throws TemplateException {
        var template = "Template result. ID: {{id}}, Name: {{name}}";
        var object = new JSONObject("{\"id\":\"id\",\"name\":\"name\"}");
        var pebble = new PebbleTemplate();
        var result = pebble.build(template, object);

        assertThat(result, is("Template result. ID: id, Name: name"));
    }

    @Test
    public void testBuild_ThrowTemplateException() throws TemplateException {
        var template = "Template result. ID: {{id}}, Name: {{name}}";
        var object = mock(JSONObject.class);
        doThrow(RuntimeException.class).when(object).keys();
        var pebble = new PebbleTemplate();

        var ex = assertThrows(TemplateException.class, () -> pebble.build(template, object));
        assertThat(ex.getMessage(), is("Cannot load template."));
        assertThat(ex.getCause(), instanceOf(RuntimeException.class));
    }
}
