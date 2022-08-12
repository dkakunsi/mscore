package com.devit.mscore.web.jersey;

import static com.devit.mscore.ApplicationContext.getContext;
import static com.devit.mscore.util.Utils.ACTION;
import static com.devit.mscore.util.Utils.AUTHORIZATION;
import static com.devit.mscore.util.Utils.BREADCRUMB_ID;
import static com.devit.mscore.util.Utils.PRINCIPAL;

import java.util.Map;

public interface Requester {

    default Map<String, String> buildRequestHeader(Map<String, String> headers) {
        var context = getContext();
        if (context == null) {
            return headers;
        }

        headers.put(BREADCRUMB_ID, context.getBreadcrumbId());
        context.getAction().ifPresent(action -> headers.put(ACTION, action));
        context.getToken().ifPresent(token -> headers.put(AUTHORIZATION, token));

        // principal can be empty when the request is login request.
        context.getPrincipal().ifPresent(principal -> headers.put(PRINCIPAL, principal.toString()));

        return headers;
    }
}
