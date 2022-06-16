package com.devit.mscore.web.javalin;

import static com.devit.mscore.util.JsonUtils.isNotJsonString;
import static com.devit.mscore.util.Utils.BREADCRUMB_ID;
import static com.devit.mscore.util.Utils.PRINCIPAL;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.devit.mscore.ApplicationContext;
import com.devit.mscore.Validation;
import com.devit.mscore.exception.ApplicationException;
import com.devit.mscore.exception.ApplicationRuntimeException;
import com.devit.mscore.exception.AuthenticationException;
import com.devit.mscore.exception.AuthorizationException;
import com.devit.mscore.exception.ConfigException;
import com.devit.mscore.exception.DataDuplicationException;
import com.devit.mscore.exception.DataException;
import com.devit.mscore.exception.DataNotFoundException;
import com.devit.mscore.exception.ImplementationException;
import com.devit.mscore.exception.SynchronizationException;
import com.devit.mscore.exception.TransformationException;
import com.devit.mscore.exception.ValidationException;
import com.devit.mscore.exception.WebClientException;
import com.devit.mscore.web.Endpoint;
import com.devit.mscore.web.Server;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.javalin.Javalin;
import io.javalin.core.JavalinConfig;
import io.javalin.http.ExceptionHandler;

/**
 * Javalin implementation of web service.
 * 
 * @author dkakunsi
 */
public final class JavalinServer extends Server {

    private static final Logger LOG = LoggerFactory.getLogger(JavalinServer.class);

    private static final String AUTHORIZATION = "Authorization";

    private Javalin app;

    private Consumer<JavalinConfig> configurer;

    private Map<Class<Exception>, ExceptionHandler<Exception>> exceptionHandler;

    JavalinServer(Integer port, List<Endpoint> endpoints, Consumer<JavalinConfig> configurer) {
        super(port, endpoints);
        this.configurer = configurer;
        this.exceptionHandler = new HashMap<>();
        setExceptionHandlers();
    }

    private void setExceptionHandlers() {
        addExceptionHandler(AuthenticationException.class, 401);
        addExceptionHandler(AuthorizationException.class, 403);
        addExceptionHandler(ValidationException.class, 400);
        addExceptionHandler(ImplementationException.class, 501);
        addExceptionHandler(DataNotFoundException.class, 404);
        addExceptionHandler(DataDuplicationException.class, 400);

        addExceptionHandler(DataException.class, 500);
        addExceptionHandler(ApplicationException.class, 500);
        addExceptionHandler(ApplicationRuntimeException.class, 500);
        addExceptionHandler(ConfigException.class, 500);
        addExceptionHandler(SynchronizationException.class, 500);
        addExceptionHandler(TransformationException.class, 500);
        addExceptionHandler(WebClientException.class, 500);
        addExceptionHandler(Exception.class, 500);
    }

    public <T extends Exception> void addExceptionHandler(Class<T> type, int statusCode) {
        addExceptionHandler(type, (ex, ctx) -> {
            LOG.error(ex.getMessage(), ex);
            ctx.status(statusCode).contentType("application/json").result(createResponseMessage(ex, statusCode).toString());
        });
    }

    @SuppressWarnings("unchecked")
    public <T extends Exception> void addExceptionHandler(Class<T> type, ExceptionHandler<T> handler) {
        this.exceptionHandler.put((Class<Exception>) type, (ExceptionHandler<Exception>) handler);
    }

    @Override
    public void start() {
        this.app = Javalin.create(getConfigurer()).start(this.port);

        initAuthentication();
        initBodyValidation();
        initEndpoint();
        initTypeAdapter();
    }

    private Consumer<JavalinConfig> getConfigurer() {
        return (this.configurer == null) ? config -> {
        } : this.configurer;
    }

    @Override
    public void stop() {
        this.app.stop();
    }

    private void initAuthentication() {
        if (this.authenticationProvider == null) {
            return;
        }

        var secureUri = this.authenticationProvider.getUri();
        if (secureUri.isEmpty()) {
            return;
        }

        secureUri.forEach((uri, role) -> this.app.before(uri, ctx -> {
                if (isPreflightRequest(ctx.method())) {
                    return;
                }

                var applicationContext = JavalinApplicationContext.of(ctx);
                var sessionKey = ctx.header(AUTHORIZATION);
                var principal = this.authenticationProvider.verify(applicationContext, sessionKey);
                if (principal == null) {
                    throw new AuthenticationException("Not authenticated.");
                }

                ctx.req.setAttribute(PRINCIPAL, principal.toString());
                ctx.req.setAttribute(BREADCRUMB_ID, applicationContext.getBreadcrumbId());
                applicationContext = JavalinApplicationContext.of(ctx);
                LOG.debug("BreadcrumbId: {}. {}", applicationContext.getBreadcrumbId(), applicationContext);
                var requiredRole = getRequiredRole(role, ctx.method());
                if (StringUtils.isNotBlank(requiredRole) && !applicationContext.hasRole(requiredRole)) {
                    throw new AuthorizationException("Not authorized.");
                }
            })
        );
    }

    private static boolean isPreflightRequest(String method) {
        return StringUtils.equalsAnyIgnoreCase(method, "options");
    }

    @SuppressWarnings("rawtypes")
    private static String getRequiredRole(Object object, String method) {
        var requiredRole = "";
        if (object instanceof String) {
            requiredRole = (String) object;
        } else if (object instanceof Map) {
            var forMethod = ((Map) object).get(method);
            requiredRole = forMethod != null ? forMethod.toString() : "";
        }
        return requiredRole;
    }

    private void initBodyValidation() {
        this.app.before(ctx -> {
            var applicationContext = JavalinApplicationContext.of(ctx);
            ctx.req.setAttribute(BREADCRUMB_ID, applicationContext.getBreadcrumbId());

            if (!isValidatable(ctx.method(), ctx.path())) {
                return;
            }

            var body = ctx.body();
            if (isNotJsonString(body)) {
                throw new ValidationException("Unexpected format.");
            }

            if (!isValid(applicationContext, this.validations, new JSONObject(body))) {
                throw new ValidationException("Invalid data. Please check log for detail.");
            }
        });
    }

    private static boolean isValidatable(String method, String path) {
        if (!StringUtils.equalsAnyIgnoreCase(method, "post", "put")) {
            // Only POST & PUT has body.
            return false;
        }
        if (StringUtils.containsAny(path.toLowerCase(), "search", "sync")) {
            // Do not validate search and sync operation
            return false;            
        }
        return true;
    }

    private static boolean isValid(ApplicationContext context, List<Validation> validations, JSONObject json) {
        return validations == null || validations.stream().allMatch(validation -> validation.validate(context, json));
    }

    private void initEndpoint() {
        if (this.endpoints.isEmpty()) {
            throw new ApplicationRuntimeException("No endpoint provided");
        }
        this.app.routes(() -> {
            for (var endpoint : this.endpoints) {
                endpoint.register();
            }
        });
    }

    private void initTypeAdapter() {
        for (var entry : this.exceptionHandler.entrySet()) {
            this.app.exception(entry.getKey(), entry.getValue());
        }
    }
}