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
        this.service.save(new JSONObject());
    }

    @Test(expected = ImplementationException.class)
    public void testDelete() throws ApplicationException {
        this.service.delete("id");
    }

    @Test(expected = ImplementationException.class)
    public void testFindId() throws ApplicationException {
        this.service.find("id");
    }

    @Test(expected = ImplementationException.class)
    public void testFindCode() throws ApplicationException {
        this.service.findByCode("code");
    }

    @Test(expected = ImplementationException.class)
    public void testFindList() throws ApplicationException {
        this.service.find(List.of());
    }

    @Test(expected = ImplementationException.class)
    public void testAll() throws ApplicationException {
        this.service.all();
    }

    @Test(expected = ImplementationException.class)
    public void testSearch() throws ApplicationException {
        this.service.search(new JSONObject());
    }
}
