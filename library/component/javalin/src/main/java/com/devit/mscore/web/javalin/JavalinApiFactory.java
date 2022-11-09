package com.devit.mscore.web.javalin;

import com.devit.mscore.AuthenticationProvider;
import com.devit.mscore.Configuration;
import com.devit.mscore.Service;
import com.devit.mscore.Validation;
import com.devit.mscore.exception.ApplicationRuntimeException;
import com.devit.mscore.exception.ConfigException;
import com.devit.mscore.web.Endpoint;
import com.devit.mscore.web.Server;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import io.javalin.core.JavalinConfig;

/**
 *
 * @author dkakunsi
 */
public class JavalinApiFactory {

  private static final String DEFAULT_PORT = "2000";

  private static final String DEFAULT_CACHE_SIZE = "1000000";

  private static final String PORT = "services.%s.web.port";

  private static final String REQUEST_CACH_SIZE = "services.%s.web.requestCacheSize";

  private static final String ENVIRONMENT = "services.%s.web.environment";

  private static final String CORS_ORIGINS = "services.%s.web.cors.origins";

  protected AuthenticationProvider authenticationProvider;

  protected List<Validation> validations;

  protected List<Endpoint> endpoints;

  private Configuration configuration;

  private Consumer<JavalinConfig> javalinConfigurer;

  protected JavalinApiFactory(Configuration configuration) {
    this.configuration = configuration;
    javalinConfigurer = createJavalinConfig();
    endpoints = new ArrayList<>();
  }

  public static JavalinApiFactory of(Configuration configuration) {
    return new JavalinApiFactory(configuration);
  }

  public static JavalinApiFactory of(Configuration configuration, AuthenticationProvider authentication,
      List<Validation> validations) {
    var manager = of(configuration);
    manager.authenticationProvider = authentication;
    manager.validations = validations;
    return manager;
  }

  private Consumer<JavalinConfig> createJavalinConfig() {
    return config -> {

      try {
        config.requestCacheSize = Long.valueOf(getRequestCacheSize());

        getEnvironment().ifPresent(env -> {
          if ("test".equals(env) || "local".equals(env)) {
            config.enableCorsForAllOrigins();
          }
        });

        getOrigins().ifPresent(origin -> config.enableCorsForOrigin(origin.split(",")));
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
    endpoints.add(endpoint);
    return this;
  }

  public Server server() throws ConfigException {
    return new JavalinServer(Integer.parseInt(getPort()), endpoints, validations,
        authenticationProvider, javalinConfigurer);
  }

  private String getPort() throws ConfigException {
    return getFormattedConfig(PORT).orElse(DEFAULT_PORT);
  }

  private String getRequestCacheSize() throws ConfigException {
    return getFormattedConfig(REQUEST_CACH_SIZE).orElse(DEFAULT_CACHE_SIZE);
  }

  private Optional<String> getOrigins() throws ConfigException {
    return getFormattedConfig(CORS_ORIGINS);
  }

  private Optional<String> getEnvironment() throws ConfigException {
    return getFormattedConfig(ENVIRONMENT);
  }

  private Optional<String> getFormattedConfig(String template) throws ConfigException {
    var configName = String.format(template, configuration.getServiceName());
    return configuration.getConfig(configName);
  }
}
