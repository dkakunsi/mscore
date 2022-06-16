package com.devit.mscore;

import static com.devit.mscore.util.Utils.BREADCRUMB_ID;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Application context without source of context.
 * 
 * @author dkakunsi.
 */
public class DefaultApplicationContext extends ApplicationContext {

    private final String source;

    private DefaultApplicationContext(String source, Map<String, Object> contextData) {
        super(contextData);
        this.source = source;
    }

    @Override
    public String getSource() {
        return this.source;
    }

    public static ApplicationContext of(String source) {
        return of(source, new HashMap<>());
    }

    public static ApplicationContext of(String source, Map<String, Object> contextData) {
        contextData.computeIfAbsent(BREADCRUMB_ID, key -> UUID.randomUUID().toString());
        return new DefaultApplicationContext(source, contextData);
    }
}
