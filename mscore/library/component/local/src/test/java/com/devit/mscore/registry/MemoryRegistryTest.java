package com.devit.mscore.registry;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.HashMap;
import java.util.Map;

import com.devit.mscore.ApplicationContext;
import com.devit.mscore.DefaultApplicationContext;
import com.devit.mscore.Registry;
import com.devit.mscore.exception.RegistryException;

import org.junit.Before;
import org.junit.Test;

public class MemoryRegistryTest {

    private Map<String, String> register;

    private Registry registry;

    private ApplicationContext context;

    @Before
    public void setup() {
        new MemoryRegistry("name"); // just for coverage.

        this.register = new HashMap<>();
        this.registry = new MemoryRegistry("name", this.register);
        this.context = DefaultApplicationContext.of("test");
    }

    @Test
    public void testAdd() throws RegistryException {
        this.register.clear();
        this.registry.add(this.context, "name", "service-url");

        assertThat(this.registry.all(this.context).size(), is(1));
    }

    @Test
    public void testGet() throws RegistryException {
        this.register.clear();
        this.register.put("domain", "service-url");

        var result = this.registry.get(this.context, "domain");
        assertThat(result, is("service-url"));
    }

    @Test
    public void testGetAll() throws RegistryException {
        this.register.clear();
        this.register.put("domain", "service-url");

        var results = this.registry.all(this.context);
        assertThat(results.size(), is(1));
        assertThat(results.get("domain"), is("service-url"));

        var values = this.registry.values(this.context);
        assertThat(values.get(0), is("service-url"));

        var keys = this.registry.keys(this.context);
        assertThat(keys.get(0), is("domain"));
    }

    @Test
    public void dummyTest() {
        this.registry.getName();
        this.registry.open();
        this.registry.close();
    }
}
