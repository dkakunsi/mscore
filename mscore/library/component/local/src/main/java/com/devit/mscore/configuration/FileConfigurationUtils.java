package com.devit.mscore.configuration;

import java.io.File;

import com.devit.mscore.exception.ConfigException;

import org.apache.commons.lang3.StringUtils;

/**
 * Load service based on configuration.
 * 
 * @author dkakunsi
 */
public final class FileConfigurationUtils {

    private static final String TEST_ARGUMENT = "-T";

    private static final String CUSTOM_ARGUMENT = "-C";

    private FileConfigurationUtils() {
    }

    public static FileConfiguration load(String... args) throws ConfigException {
        if (isTest(args)) {
            return loadTestConfiguration(args);
        }
        if (isCustomConfig(args)) {
            return loadCustomConfiguration(args);
        }
        return new FileConfiguration();
    }

    private static boolean isTest(String... args) {
        return args != null && args.length > 0 && args[0].startsWith(TEST_ARGUMENT);
    }

    private static FileConfiguration loadTestConfiguration(String... args) throws ConfigException {
        var fileName = loadTestConfigFile(args);
        return load(loadConfigPath(fileName));
    }

    private static String loadTestConfigFile(String... args) {
        return loadConfigFile(TEST_ARGUMENT, args);
    }

    private static boolean isCustomConfig(String... args) {
        return args != null && args.length > 0 && args[0].startsWith(CUSTOM_ARGUMENT);
    }

    private static FileConfiguration loadCustomConfiguration(String... args) throws ConfigException {
        var fileName = loadCustomConfigFile(args);
        return load(loadConfigPath(fileName));
    }

    private static String loadCustomConfigFile(String... args) {
        return loadConfigFile(CUSTOM_ARGUMENT, args);
    }

    private static String loadConfigFile(String configOption, String... args) {
        return args[0].replace(configOption, "");
    }

    private static String loadConfigPath(String fileName) {
        if (StringUtils.isEmpty(fileName)) {
            return null;
        }
        var file = new File(fileName);
        return file.getPath();
    }

    public static FileConfiguration load(String configFile) throws ConfigException {
        if (StringUtils.isEmpty(configFile)) {
            return new FileConfiguration();
        }
        return new FileConfiguration(configFile);
    }
}