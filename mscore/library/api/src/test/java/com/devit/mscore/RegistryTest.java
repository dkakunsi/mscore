package com.devit.mscore;

import static org.hamcrest.core.Is.is;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;

import com.devit.mscore.exception.RegistryException;

import org.junit.Test;

public class RegistryTest {

    @Test
    public void test_getName() {
        var registry = new DummyRegistry();
        assertThat(registry.getName(), is("dummy"));
    }

    private static class DummyRegistry implements Registry {

        @Override
        public String getName() {
            return "dummy";
        }

        @Override
        public void add(String key, String value) {
        }

        @Override
        public String get(String key) {
            return null;
        }

        @Override
        public Map<String, String> all() throws RegistryException {
            return null;
        }

        @Override
        public void open() {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void close() {
            // TODO Auto-generated method stub
            
        }

        @Override
        public List<String> values() throws RegistryException {
            return null;
        }

        @Override
        public List<String> keys() throws RegistryException {
            return null;
        }
    }
}
