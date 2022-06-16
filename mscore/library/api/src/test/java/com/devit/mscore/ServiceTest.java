package com.devit.mscore;

import java.util.List;

import com.devit.mscore.exception.ApplicationException;
import com.devit.mscore.exception.ImplementationException;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

public class ServiceTest {

    private Service service;

    @Before
    public void setup() {
        this.service = new Service() {
            @Override
            public String getDomain() {
                return "domain";
            }
        };
    }

    @Test(expected = ImplementationException.class)
    public void testSave() throws ApplicationException {
        this.service.save(DefaultApplicationContext.of("test"), new JSONObject());
    }

    @Test(expected = ImplementationException.class)
    public void testDelete() throws ApplicationException {
        this.service.delete(DefaultApplicationContext.of("test"), "id");
    }

    @Test(expected = ImplementationException.class)
    public void testFindId() throws ApplicationException {
        this.service.find(DefaultApplicationContext.of("test"), "id");
    }

    @Test(expected = ImplementationException.class)
    public void testFindCode() throws ApplicationException {
        this.service.findByCode(DefaultApplicationContext.of("test"), "code");
    }

    @Test(expected = ImplementationException.class)
    public void testFindList() throws ApplicationException {
        this.service.find(DefaultApplicationContext.of("test"), List.of());
    }

    @Test(expected = ImplementationException.class)
    public void testAll() throws ApplicationException {
        this.service.all(DefaultApplicationContext.of("test"));
    }

    @Test(expected = ImplementationException.class)
    public void testSearch() throws ApplicationException {
        this.service.search(DefaultApplicationContext.of("test"), new JSONObject());
    }
}
