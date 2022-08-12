package com.devit.mscore.configuration;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;

import java.io.File;

import com.devit.mscore.exception.ConfigException;

import org.junit.Test;

public class FileConfigurationTest {

    @Test
    public void testDummy() throws ConfigException {
        assertThrows(ConfigException.class, ()-> new FileConfiguration());
    }

    @Test
    public void testGetServiceName() throws ConfigException {
        var configLocation = getLocation("config");
        var configuration = new FileConfiguration(configLocation);
        assertThat(configuration.getServiceName(), is("test"));
    }

    @Test
    public void testGetConfig_ApplicationContext() throws ConfigException {
        var configLocation = getLocation("config");
        var configuration = new FileConfiguration(configLocation);
        var configValue = configuration.getConfig("platform.mongo.host");
        assertThat(configValue.isPresent(), is(true));
        assertThat(configValue.get(), is("mongo-host"));
    }

    private String getLocation(String location) {
        var classLoader = getClass().getClassLoader();
        var resource = classLoader.getResource(location);
        var file = new File(resource.getFile());
        return file.getAbsolutePath();
    }
}
