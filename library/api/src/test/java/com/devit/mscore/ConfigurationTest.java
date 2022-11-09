package com.devit.mscore;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertTrue;

import com.devit.mscore.exception.ConfigException;

import java.util.Map;

import org.junit.Test;

public class ConfigurationTest {

  @Test
  public void testGetPrefixedConfig_RemovePrefix() {
    var configuration = new ConfigurationImpl();
    var prefixedConfigs = configuration.getPrefixedConfig("prefix", true);
    assertTrue(prefixedConfigs.containsKey("config1"));
    assertThat(prefixedConfigs.get("config1"), is("value1"));
    assertTrue(prefixedConfigs.containsKey("config2"));
    assertThat(prefixedConfigs.get("config2"), is("value2"));
  }

  @Test
  public void testGetPrefixedConfig_DontRemovePrefix() {
    var configuration = new ConfigurationImpl();
    var prefixedConfigs = configuration.getPrefixedConfig("prefix", false);
    assertTrue(prefixedConfigs.containsKey("prefix.config1"));
    assertThat(prefixedConfigs.get("prefix.config1"), is("value1"));
    assertTrue(prefixedConfigs.containsKey("prefix.config2"));
    assertThat(prefixedConfigs.get("prefix.config2"), is("value2"));
  }

  @Test
  public void testGetConfigInt() throws ConfigException {
    var configuration = new ConfigurationImpl();
    var optInt = configuration.getConfigInt("integer");
    assertTrue(optInt.isPresent());
    assertThat(optInt.get(), is(0));
  }

  @Test
  public void testGetConfigLong_GetException_ShouldBeDefaultValue() throws ConfigException {
    var configuration = new ConfigurationImpl();
    var optInt = configuration.getConfigLong("long");
    assertTrue(optInt.isPresent());
    assertThat(optInt.get(), is(0L));
  }

  @Test
  public void testHas() {
    var configuration = new ConfigurationImpl();
    assertTrue(configuration.has("prefix.config1"));
  }

  @Deprecated
  @Test
  public void testDummy() throws ConfigException {
    var configuration = new ConfigurationImpl();
    var valueConfig = configuration.getConfig("key");
    assertTrue(valueConfig.isEmpty());
    var optionalConfig = configuration.getConfig("type", "service", "key");
    assertTrue(optionalConfig.isEmpty());
    optionalConfig = configuration.getConfig("path");
    assertTrue(optionalConfig.isEmpty());
  }

  public class ConfigurationImpl implements Configuration {

    private Map<String, String> map;

    public ConfigurationImpl() {
      this.map = Map.of("prefix.config1", "value1", "prefix.config2", "value2", "config3", "value3", "integer", "0", "long", "long");
    }

    @Override
    public Map<String, String> getConfigs() {
      return this.map;
    }

    @Override
    public String getPrefixSeparator() {
      return ".";
    }

    @Override
    public String getServiceName() {
      return "test";
    }
  }
}
