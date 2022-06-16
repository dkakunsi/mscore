package com.devit.mscore.util;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

public class JsonUtilTest {

    @Test
    public void testCopy() {
        var source = new JSONObject();
        source.put("keyA", "Value A1");
        source.put("keyB", "Value B1");
        source.put("keyC", "Value C1");

        var destination = new JSONObject();
        destination.put("keyA", "Value A");
        destination.put("keyB", "Value B");
        destination.put("keyC", "Value C");
        destination.put("keyD", "Value D");

        JsonUtils.copy(destination, source);

        assertThat(destination.get("keyA"), is("Value A1"));
        assertThat(destination.get("keyB"), is("Value B1"));
        assertThat(destination.get("keyC"), is("Value C1"));
        assertThat(destination.get("keyD"), is("Value D"));
    }

    @Test
    public void testCopy_EmptyJson() {
        var source = new JSONObject();

        var destination = new JSONObject();
        destination.put("keyA", "Value A");
        destination.put("keyB", "Value B");
        destination.put("keyC", "Value C");
        destination.put("keyD", "Value D");

        JsonUtils.copy(destination, source);

        assertThat(destination.get("keyA"), is("Value A"));
        assertThat(destination.get("keyB"), is("Value B"));
        assertThat(destination.get("keyC"), is("Value C"));
        assertThat(destination.get("keyD"), is("Value D"));
    }

    @Test
    public void testCopy_Removing() {
        var source = new JSONObject();
        source.put("jsonNull", JSONObject.NULL);
        source.put("nullString", "null");
        source.put("emptyObject", new JSONObject());
        source.put("emptyArray", new JSONArray());

        var destination = new JSONObject();
        destination.put("jsonNull", "value");
        destination.put("nullString", "value");
        destination.put("emptyObject", "value");
        destination.put("emptyArray", "value");

        JsonUtils.copy(destination, source);

        assertTrue(destination.isEmpty());
    }

    @Test
    public void testIsNotJsonString() {
        assertTrue(JsonUtils.isNotJsonString("abc"));
    }

    @Test
    public void testFlatten() {
        var nestedObject = new JSONObject();
        nestedObject.put("nestedString", "Nested");
        nestedObject.put("nestedFloat", 1.1);

        var nestedArray = new JSONArray();
        nestedArray.put(nestedObject);
        nestedArray.put("Array String Value");
        nestedArray.put(1);
        nestedArray.put(1.1);
        
        var objectToFlatten = new JSONObject();
        objectToFlatten.put("stringName", "Name");
        objectToFlatten.put("intNumber", 1);
        objectToFlatten.put("nestedObject", nestedObject);
        objectToFlatten.put("nestedArray", nestedArray);

        var target = JsonUtils.flatten(objectToFlatten);
        assertThat(target.getString("stringName"), is("Name"));
        assertThat(target.getInt("intNumber"), is(1));
        assertThat(target.getString("nestedObject_nestedString"), is("Nested"));
        assertThat(target.getFloat("nestedObject_nestedFloat"), is(1.1f));
        assertThat(target.getString("nestedArray_0_nestedString"), is("Nested"));
        assertThat(target.getFloat("nestedArray_0_nestedFloat"), is(1.1f));
        assertThat(target.getString("nestedArray_1"), is("Array String Value"));
        assertThat(target.getInt("nestedArray_2"), is(1));
        assertThat(target.getFloat("nestedArray_3"), is(1.1f));
    }
}
