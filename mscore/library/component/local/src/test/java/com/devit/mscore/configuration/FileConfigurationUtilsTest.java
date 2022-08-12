package com.devit.mscore.configuration;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;

import com.devit.mscore.exception.ConfigException;

import org.junit.Test;

public class FileConfigurationUtilsTest {

    @Test
    public void testPrefixSeparator() throws ConfigException {
        var arg = getLocation("config");
        var config = FileConfigurationUtils.load(arg);

        assertNotNull(config);
        assertThat(config.getPrefixSeparator(), is("."));
    }

    @Test
    public void testLoadArgs_CustomConfig() throws ConfigException {
        String[] arg = { "-C" + getLocation("config") };
        var config = FileConfigurationUtils.load(arg);

        assertNotNull(config);
        assertThat(config.getConfigs().size(), is(4));
        assertThat(config.getConfig("platform.mongo.host").get(), is("mongo-host"));
        assertThat(config.getConfig("services.data.schema.resource.location").get(), is("./schema"));
        assertThat(config.getConfig("services.workflow.db.name").get(), is("workflow"));
    }

    @Test
    public void testLoadArgs_TestConfig() throws ConfigException {
        String[] arg = { "-T" + getLocation("config") };
        var config = FileConfigurationUtils.load(arg);

        assertNotNull(config);
        assertThat(config.getConfigs().size(), is(4));
        assertThat(config.getConfig("platform.mongo.host").get(), is("mongo-host"));
        assertThat(config.getConfig("services.data.schema.resource.location").get(), is("./schema"));
        assertThat(config.getConfig("services.workflow.db.name").get(), is("workflow"));
    }

    @Test
    public void testLoad() throws ConfigException {
        var arg = getLocation("config");
        var config = FileConfigurationUtils.load(arg);

        assertNotNull(config);
        assertThat(config.getConfigs().size(), is(4));
        assertThat(config.getConfig("platform.mongo.host").get(), is("mongo-host"));
        assertThat(config.getConfig("services.data.schema.resource.location").get(), is("./schema"));
        assertThat(config.getConfig("services.workflow.db.name").get(), is("workflow"));
    }

    @Test
    public void testLoad_WithEmptyValue() throws ConfigException {
        var arg = getLocation("configWithEmptyValue");
        var config = FileConfigurationUtils.load(arg);

        assertNotNull(config);
        assertThat(config.getConfigs().size(), is(3));
        assertThat(config.getConfig("platform.mongo.host").get(), is("mongo-host"));
        assertThat(config.getConfig("services.data.schema.resource.location").isEmpty(), is(true));
        assertThat(config.getConfig("services.workflow.db.name").get(), is("workflow"));
    }

    @Test
    public void testLoad_ThrowException() throws ConfigException {
        var arg = getLocation("invalidconfig");
        var ex = assertThrows(ConfigException.class, () -> FileConfigurationUtils.load(arg));
        assertThat(ex.getMessage(), is("Cannot read invalid config"));
    }

    private String getLocation(String location) {
        var classLoader = getClass().getClassLoader();
        var resource = classLoader.getResource(location);
        var file = new File(resource.getFile());
        return file.getAbsolutePath();
    }
}
