package com.devit.mscore;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
import org.junit.Test;

public class ApplicationContextTest {

    @Test
    public void testContext() {
        var appContext = DefaultApplicationContext.of("test");
        ApplicationContext.setContext(appContext);
        var retrievedContext = ApplicationContext.getContext();
        assertThat(retrievedContext.getSource(), is(appContext.getSource()));
    }

    @Test
    public void testDataAvailable() {
        // @formatter:off
        var contextData = Map.of(
                "breadcrumbId", "breadcrumbId",
                "principal", new JSONObject("{\"requestedBy\":\"requestedBy\",\"role\":[\"user\"]}"),
                "action", "domain.action",
                "Authorization", "JWT Token");
        // @formatter:on

        var context = DefaultApplicationContext.of("test", new HashMap<>(contextData));
        assertThat(context.getBreadcrumbId(), is("breadcrumbId"));
        assertThat(context.getRequestedBy(), is("requestedBy"));
        assertThat(context.getAction().get(), is("domain.action"));
        assertThat(context.getToken().get(), is("JWT Token"));
        assertTrue(context.hasAction());
        assertTrue(context.getPrincipal().isPresent());
        assertTrue(context.hasRole("user"));
        assertFalse(context.hasRole("admin"));
        assertNotNull(context.toJson());
        assertNotNull(context.toString());
    }

    @Test
    public void testDataNotAvailable() {
        var context = DefaultApplicationContext.of("test");
        assertNotNull(context.getBreadcrumbId());
        assertThat(context.getRequestedBy(), is("UNKNOWN"));
        assertFalse(context.getAction().isPresent());
        assertFalse(context.getToken().isPresent());
        assertFalse(context.hasAction());
        assertFalse(context.getPrincipal().isPresent());
        assertFalse(context.hasRole("user"));
        assertNotNull(context.toJson());
        assertNotNull(context.toString());
    }

    @Test
    public void testDataFromSubclass() {
        var principal = "{\"requestedBy\":\"requestedBy\",\"role\":[\"user\"]}";

        var context = DefaultApplicationContext.of("test");
        context.setPrincipal(principal);
        context.setBreadcrumbId("breadcrumbId");

        assertNotNull(context.getBreadcrumbId());
        assertThat(context.getRequestedBy(), is("requestedBy"));
        assertTrue(context.hasRole("user"));
    }

    @Test
    public void testDataFromSubclass_WithGeneratingBreadcrumbId() {
        var principal = "{\"requestedBy\":\"requestedBy\",\"role\":[\"user\"]}";

        var context = DefaultApplicationContext.of("test");
        context.setPrincipal(principal);
        context.generateBreadcrumbId();

        assertNotNull(context.getBreadcrumbId());
        assertThat(context.getRequestedBy(), is("requestedBy"));
        assertTrue(context.hasRole("user"));
    }}
