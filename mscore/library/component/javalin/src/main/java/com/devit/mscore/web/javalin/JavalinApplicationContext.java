package com.devit.mscore.web.javalin;

import static com.devit.mscore.util.Utils.ACTION;
import static com.devit.mscore.util.Utils.AUTHORIZATION;
import static com.devit.mscore.util.Utils.BREADCRUMB_ID;
import static com.devit.mscore.util.Utils.PRINCIPAL;

import java.util.HashMap;
import java.util.Map;

import com.devit.mscore.ApplicationContext;

import org.apache.commons.lang3.StringUtils;

import io.javalin.http.Context;

public class JavalinApplicationContext extends ApplicationContext {

    private JavalinApplicationContext(Map<String, Object> contextData) {
        super(contextData);
    }

    public static ApplicationContext of(Context ctx) {
        var contextData = new HashMap<String, Object>();
        contextData.putAll(ctx.headerMap());

        var context = new JavalinApplicationContext(contextData);
        context.principal(ctx);
        context.breadcrumbId(ctx);
        context.action(ctx);
        context.authorization(ctx);

        return context;
    }

    private void breadcrumbId(Context ctx) {
        var breadcrumbId = getValue(ctx, BREADCRUMB_ID);
        if (StringUtils.isNotBlank(breadcrumbId)) {
            setBreadcrumbId(breadcrumbId);
        }
    }

    private void principal(Context ctx) {
        var principal = getValue(ctx, PRINCIPAL);
        if (principal != null && StringUtils.isNotBlank(principal)) {
            setPrincipal(principal);
        }
    }

    private String getValue(Context ctx, String key) {
        var value = ctx.attribute(key);
        if (value == null || StringUtils.isBlank(value.toString())) {
            value = ctx.header(key);
        }
        return value != null ? value.toString() : null;
    }

    private void action(Context ctx) {
        var action = ctx.header(ACTION);
        if (StringUtils.isNotBlank(action)) {
            this.contextData.put(ACTION, action);
        }
    }

    private void authorization(Context ctx) {
        var authorization = ctx.header(AUTHORIZATION);
        if (StringUtils.isNotBlank(authorization)) {
            this.contextData.put(AUTHORIZATION, authorization);
        }
    }

    @Override
    public String getSource() {
        return "web";
    }
}