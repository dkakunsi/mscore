package com.devit.mscore;

import static java.net.InetAddress.getLocalHost;

import com.devit.mscore.exception.ApplicationRuntimeException;
import com.devit.mscore.exception.ConfigException;
import com.devit.mscore.exception.RegistryException;

import java.net.UnknownHostException;
import java.util.function.Supplier;

/**
 * Register a service into the platform registry.
 *
 * @author dkakunsi
 */
public class ServiceRegistration implements Cloneable {

  private static final String REGISTRY_STATIC = "platform.service.registry.static";

  private static final String REGISTRY_ADDRESS = "services.%s.registry.address";

  private static final String REGISTRY_PROTOCOL = "platform.service.registry.protocol";

  private static final String WEB_PORT = "platform.service.web.port";

  private static final String DEFAULT_PROTOCOL = "http";

  private static final String KEY_FORMAT = "/services/endpoint/%s/url";

  private final Registry registry;

  private final Configuration configuration;

  public ServiceRegistration(Registry registry, Configuration configuration) {
    try {
      this.registry = (Registry) registry.clone();
      this.configuration = configuration;
    } catch (CloneNotSupportedException ex) {
      throw new ApplicationRuntimeException(ex);
    }
  }

  /**
   * Register a service to platform registry. Register with it's domain name.
   *
   * @param service service to register.
   * @throws RegistryException cannot register service.
   */
  public void register(Service service) throws RegistryException {
    register(service.getDomain());
  }

  /**
   * Register domain address to platform registry.
   *
   * @param domain domain name to register.
   * @throws ConfigException cannot register service.
   */
  public void register(String domain) throws RegistryException {
    try {
      executeRegister(domain);
    } catch (ConfigException ex) {
      throw new RegistryException(ex);
    }
  }

  private void executeRegister(String domain) throws ConfigException, RegistryException {
    var useStaticAddressOptional = this.configuration.getConfig(REGISTRY_STATIC).orElse("false");
    var useStaticAddress = Boolean.parseBoolean(useStaticAddressOptional);
    var address = getAddress(useStaticAddress, domain);
    var key = getKey(domain);
    this.registry.add(key, address);
  }

  private String getAddress(boolean useStaticAddress, String domain)
      throws ConfigException, RegistryException {
    if (useStaticAddress) {
      var configKey = String.format(REGISTRY_ADDRESS, this.configuration.getServiceName());
      Supplier<ConfigException> throwingElse = () -> new ConfigException("No config for registry address");
      var baseAddress = this.configuration.getConfig(configKey).orElseThrow(throwingElse);
      return String.format("%s/%s", baseAddress, domain);
    } else {
      Supplier<ConfigException> throwingElse = () -> new ConfigException("No config for web port");
      var protocol = getProtocol(this.configuration);
      var localAddress = getLocalAddress();
      var port = this.configuration.getConfig(WEB_PORT).orElseThrow(throwingElse);
      return String.format("%s://%s:%s/%s", protocol, localAddress, port, domain);
    }
  }

  public String get(String domain) throws RegistryException {
    return this.registry.get(getKey(domain));
  }

  private static String getKey(String domain) {
    return String.format(KEY_FORMAT, domain);
  }

  private static String getLocalAddress() throws RegistryException {
    try {
      return getLocalHost().getHostAddress();
    } catch (UnknownHostException ex) {
      throw new RegistryException("Cannot retrieve local address", ex);
    }
  }

  private static String getProtocol(Configuration configuration) throws ConfigException {
    return configuration.getConfig(REGISTRY_PROTOCOL).orElse(DEFAULT_PROTOCOL);
  }

  public void open() {
    this.registry.open();
  }

  public void close() {
    this.registry.close();
  }

  @Override
  public Object clone() throws CloneNotSupportedException {
    return super.clone();
  }
}
