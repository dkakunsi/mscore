package com.devit.mscore;

import com.devit.mscore.exception.ConfigException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * <p>
 * Interface to read configuration. The implementor should handle different type
 * of configuration source.
 * </p>
 *
 * @author dkakunsi
 */
public interface Configuration {

  /**
   *
   * @return current service name.
   */
  String getServiceName();

  /**
   *
   * @return config mapping.
   */
  Map<String, String> getConfigs();

  /**
   *
   * @param key to search.
   * @return configuration value.
   * @throws ConfigException cannot get the value.
   */
  default Optional<String> getConfig(String key) throws ConfigException {
    return Optional.empty();
  }

  /**
   *
   * @param type    of configuration
   * @param service configuration
   * @param key     of configuration
   * @return optional value
   * @throws ConfigException cannot get the value.
   */
  default Optional<String> getConfig(String type, String service, String key) throws ConfigException {
    return Optional.empty();
  }

  /**
   *
   * @param prefix       to load.
   * @param removePrefix remove the prefix in the returned mapping.
   * @return config mapping for specified prefix
   */
  default Map<String, String> getPrefixedConfig(String prefix, boolean removePrefix) {
    var prefixedConfig = new HashMap<String, String>();

    for (var config : getConfigs().entrySet()) {
      if (config.getKey().startsWith(prefix)) {
        var configName = config.getKey();
        if (removePrefix) {
          configName = configName.replace(prefix + getPrefixSeparator(), "");
        }
        prefixedConfig.put(configName, config.getValue());
      }
    }

    return prefixedConfig;
  }

  /**
   *
   * @return separator between prefix and config name.
   */
  String getPrefixSeparator();

  /**
   * Check whether the config exists.
   *
   * @param configName configuration name.
   * @return true if exists.
   */
  default boolean has(String configName) {
    return getConfigs().containsKey(configName);
  }
}
