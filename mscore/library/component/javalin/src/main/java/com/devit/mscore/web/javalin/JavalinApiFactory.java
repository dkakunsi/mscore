package com.devit.mscore.web.javalin;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import com.devit.mscore.ApplicationContext;
import com.devit.mscore.AuthenticationProvider;
import com.devit.mscore.Configuration;
import com.devit.mscore.Service;
import com.devit.mscore.Validation;
import com.devit.mscore.exception.ApplicationRuntimeException;
import com.devit.mscore.exception.ConfigException;
import com.devit.mscore.web.Endpoint;
import com.devit.mscore.web.Server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.javalin.core.JavalinConfig;

/**
 * 
 * @author dkakunsi
 */
public class JavalinApiFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(JavalinApiFactory.class);

    private static final String DEFAULT_PORT = "2000";

    private static final String PORT = "platform.service.web.port";

    private static final String CONFIG_TEMPLATE = "services.%s.web.%s";

    private static final String ENVIRONMENT = "environment";

    private static final String CORS_ORIGINS = "cors.origins";

    protected AuthenticationProvider authenticationProvider;

    protected List<Validation> validations;

    protected List<Endpoint> endpoints;

    private Configuration configuration;

    private Consumer<JavalinConfig> javalinConfigurer;

    private Server server;

    private String serviceName;

    protected JavalinApiFactory(ApplicationContext context, Configuration configuration) {
        this.configuration = configuration;
        this.javalinConfigurer = createJavalinConfig(context);
        this.endpoints = new ArrayList<>();
        this.serviceName = configuration.getServiceName();
    }

    public static JavalinApiFactory of(ApplicationContext context, Configuration configuration) {
        return new JavalinApiFactory(context, configuration);
    }

    public static JavalinApiFactory of(ApplicationContext context, Configuration configuration, AuthenticationProvider authentication,
            List<Validation> validations) {
        var manager = of(context, configuration);
        manager.authenticationProvider = authentication;
        manager.validations = validations;
        return manager;
    }

    private Consumer<JavalinConfig> createJavalinConfig(ApplicationContext context) {
        return config -> {

            try {
                var requestCacheSize = configuration.getConfig(context, "javalin.requestCacheSize").orElse("1000000");
                config.requestCacheSize = Long.valueOf(requestCacheSize);

                getEnvironment(context).ifPresent(env -> {
                    if ("test".equals(env)) {
                        config.enableCorsForAllOrigins();
                        LOGGER.info("Add cors for all origins.");
                    }
                });

                getOrigins(context).ifPresent(origin -> config.enableCorsForOrigin(origin.split(",")));
            } catch (ConfigException ex) {
                throw new ApplicationRuntimeException(ex);
            }
        };
    }

    public JavalinApiFactory add(Service service) {
        add(new JavalinEndpoint(service));
        return this;
    }

    public JavalinApiFactory add(JavalinController controller) {
        add(new JavalinEndpoint(controller));
        return this;
    }

    public JavalinApiFactory add(Endpoint endpoint) {
        this.endpoints.add(endpoint);
        return this;
    }

    public Server server(ApplicationContext context) throws ConfigException {
        if (this.server == null) {
            initServer(context);
        }
        return this.server;
    }

    private void initServer(ApplicationContext context) throws ConfigException {
        this.server = new JavalinServer(Integer.parseInt(getPort(context)), this.endpoints, this.javalinConfigurer);
        this.server.setAuthenticationProvider(this.authenticationProvider);
        this.server.setValidations(this.validations);
    }

    private String getPort(ApplicationContext context) throws ConfigException {
        return this.configuration.getConfig(context, PORT).orElse(DEFAULT_PORT);
    }

    private Optional<String> getOrigins(ApplicationContext context) throws ConfigException {
        var configName = String.format(CONFIG_TEMPLATE, this.serviceName, CORS_ORIGINS);
        return this.configuration.getConfig(context, configName);
    }

    private Optional<String> getEnvironment(ApplicationContext context) throws ConfigException {
        var configName = String.format(CONFIG_TEMPLATE, this.serviceName, ENVIRONMENT);
        return this.configuration.getConfig(context, configName);
    }
}
