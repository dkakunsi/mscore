package com.devit.mscore.web;

import static org.junit.Assert.assertNull;

import java.util.Map;
import java.util.Optional;

import com.devit.mscore.ApplicationContext;
import com.devit.mscore.exception.WebClientException;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

public class ClientTest {

    private Client client;

    @Before
    public void setup() {
        this.client = new DummyClient();
    }
    
    @Deprecated
    @Test
    public void testPost() throws WebClientException {
        var res = this.client.post("uri", null, null);
        assertNull(res);
    }

    @Deprecated
    @Test
    public void testPut() throws WebClientException {
        var res = this.client.put("uri", null, null);
        assertNull(res);
    }

    private static class DummyClient implements Client {

        @Override
        public Client createNew() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public JSONObject post(ApplicationContext context, String uri, Optional<JSONObject> payload)
                throws WebClientException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public JSONObject put(ApplicationContext context, String uri, Optional<JSONObject> payload)
                throws WebClientException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public JSONObject delete(ApplicationContext context, String uri) throws WebClientException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public JSONObject get(ApplicationContext context, String uri, Map<String, String> params)
                throws WebClientException {
            // TODO Auto-generated method stub
            return null;
        }
        
    }
}
