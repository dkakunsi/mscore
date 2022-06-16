package com.devit.mscore;

import static java.net.Inet4Address.getLocalHost;

import java.net.UnknownHostException;
import java.util.function.Supplier;

import com.devit.mscore.exception.ConfigException;
import com.devit.mscore.exception.RegistryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Register a service into the platform registry.
 * 
 * @author dkakunsi
 */
public class ServiceRegistration {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceRegistration.class);

    private static final String REGISTRY_STATIC = "platform.service.registry.static";

    private static final String REGISTRY_ADDRESS = "services.%s.registry.address";

    private static final String REGISTRY_PROTOCOL = "platform.service.registry.protocol";

    private static final String WEB_PORT = "platform.service.web.port";

    private static final String DEFAULT_PROTOCOL = "http";

    private static final String KEY_FORMAT = "/services/endpoint/%s/url";

    private final Registry registry;

    private final Configuration configuration;

    public ServiceRegistration(Registry registry, Configuration configuration) {
        this.registry = registry;
        this.configuration = configuration;
    }

    /**
     * Register a service to platform registry. Register with it's domain name.
     * 
     * @param context application context.
     * @param service service to register.
     * @throws RegistryException cannot register service.
     */
    public void register(ApplicationContext context, Service service) throws RegistryException {
        register(context, service.getDomain());
    }

    /**
     * Register domain address to platform registry.
     * 
     * @param context application context.
     * @param domain  domain name to register.
     * @throws ConfigException cannot register service.
     */
    public void register(ApplicationContext context, String domain) throws RegistryException {
        try {
            executeRegister(context, domain);
        } catch (ConfigException ex) {
            throw new RegistryException(ex);
        }
    }

    private void executeRegister(ApplicationContext context, String domain) throws ConfigException, RegistryException {
        LOG.trace("BreadcrumbId: {}. Registering {} service to platform registry.", context.getBreadcrumbId(), domain);

        var useStaticAddressOptional = this.configuration.getConfig(context, REGISTRY_STATIC).orElse("false");
        var useStaticAddress = Boolean.parseBoolean(useStaticAddressOptional);
        var address = getAddress(context, useStaticAddress, domain);
        var key = getKey(domain);
        this.registry.add(context, key, address);

        LOG.info("BreadcrumbId: {}. '{}' service is registered to platform registry as {}.", context.getBreadcrumbId(),
                domain, address);
    }

    private String getAddress(ApplicationContext context, boolean useStaticAddress, String domain)
            throws ConfigException, RegistryException {
        if (useStaticAddress) {
            var configKey = String.format(REGISTRY_ADDRESS, this.configuration.getServiceName());
            Supplier<ConfigException> throwingElse = () -> new ConfigException("No config for registry address.");
            var baseAddress = this.configuration.getConfig(context, configKey).orElseThrow(throwingElse);
            return String.format("%s/%s", baseAddress, domain);
        } else {
            Supplier<ConfigException> throwingElse = () -> new ConfigException("No config for web port.");
            var protocol = getProtocol(context, this.configuration);
            var localAddress = getLocalAddress();
            var port = this.configuration.getConfig(context, WEB_PORT).orElseThrow(throwingElse);
            return String.format("%s://%s:%s/%s", protocol, localAddress, port, domain);
        }
    }

    public String get(ApplicationContext context, String domain) throws RegistryException {
        return this.registry.get(context, getKey(domain));
    }

    private static String getKey(String domain) {
        return String.format(KEY_FORMAT, domain);
    }

    private static String getLocalAddress() throws RegistryException {
        try {
            return getLocalHost().getHostAddress();
        } catch (UnknownHostException ex) {
            throw new RegistryException("Cannot retrieve local address.", ex);
        }
    }

    private static String getProtocol(ApplicationContext context, Configuration configuration) throws ConfigException {
        return configuration.getConfig(context, REGISTRY_PROTOCOL).orElse(DEFAULT_PROTOCOL);
    }

    public void open() {
        this.registry.open();
    }

    public void close() {
        this.registry.close();
    }
}
