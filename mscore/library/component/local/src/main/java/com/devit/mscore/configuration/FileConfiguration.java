package com.devit.mscore.configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.devit.mscore.ApplicationContext;
import com.devit.mscore.Configuration;
import com.devit.mscore.exception.ConfigException;

import org.apache.commons.lang3.StringUtils;

public class FileConfiguration implements Configuration {

    private static final String DEFAULT_LOCATION = "./app.config";

    private static final String COMMENT_FLAG = "#";

    private static final String SERVICE_NAME = "service.name";

    private Map<String, String> configs;

    public FileConfiguration() throws ConfigException {
        this(DEFAULT_LOCATION);
    }

    public FileConfiguration(String configLocation) throws ConfigException {
        this.configs = readConfig(configLocation);
    }

    private static Map<String, String> readConfig(String configLocation) throws ConfigException {
        var config = new HashMap<String, String>();

        try {
            for (var line : Files.readAllLines(Paths.get(configLocation))) {
                if (StringUtils.isEmpty(line) || StringUtils.startsWith(line, COMMENT_FLAG)) {
                    continue;
                }
                if (!line.contains("=")) {
                    throw new ConfigException("Cannot read invalid config");
                }

                var elements = line.split("=");
                var value = "";
                if (elements.length > 1) {
                    value = elements[1];
                }
                config.put(elements[0], value);
            }
            return config;
        } catch (IOException | ArrayIndexOutOfBoundsException ex) {
            throw new ConfigException("Cannot read invalid config", ex);
        }
    }

    @Override
    public String getServiceName() {
        return this.configs.get(SERVICE_NAME);
    }

    @Override
    public Map<String, String> getConfigs() {
        return this.configs;
    }

    @Override
    public Optional<String> getConfig(ApplicationContext context, String key) throws ConfigException {
        var value = this.configs.get(key);
        return StringUtils.isNotBlank(value) ? Optional.of(value) : Optional.empty();
    }

    @Override
    public String getPrefixSeparator() {
        return ".";
    }
}
