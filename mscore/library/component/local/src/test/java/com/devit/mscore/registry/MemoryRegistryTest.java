package com.devit.mscore.registry;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import com.devit.mscore.exception.RegistryException;

import org.junit.Test;

public class MemoryRegistryTest {

  @Test
  public void testAddRegitry() throws RegistryException {
    var registry = new MemoryRegistry("name"); // just for coverage.

    // add
    registry.add("name", "service-url");
    assertThat(registry.all().size(), is(1));

    // get
    var result = registry.get("name");
    assertThat(result, is("service-url"));

    // all
    var results = registry.all();
    assertThat(results.size(), is(1));
    assertThat(results.get("name"), is("service-url"));
    var values = registry.values();
    assertThat(values.get(0), is("service-url"));
    var keys = registry.keys();
    assertThat(keys.get(0), is("name"));
  }
}
