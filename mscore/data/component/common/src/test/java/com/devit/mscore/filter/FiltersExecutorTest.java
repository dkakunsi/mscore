package com.devit.mscore.filter;

import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.Optional;

import com.devit.mscore.ApplicationContext;
import com.devit.mscore.Configuration;
import com.devit.mscore.exception.ConfigException;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

public class FiltersExecutorTest {

    private ApplicationContext context;

    private Configuration configuration;

    @Before
    public void setup() {
        this.context = mock(ApplicationContext.class);
        doReturn("breadcrumbId").when(this.context).getBreadcrumbId();
        this.configuration = mock(Configuration.class);
        doReturn("data").when(this.configuration).getServiceName();
    }

    @Test
    public void testFilter_Password() throws ConfigException {
        doReturn(Optional.of("password")).when(this.configuration).getConfig(any(ApplicationContext.class), eq("services.data.filter.remove"));
        var factory = FilterFactory.of();
        var executors = factory.filters(this.context, this.configuration);
        var json = new JSONObject("{\"domain\":\"domain\",\"password\":\"abcd\"}");
        executors.execute(this.context, json);

        assertFalse(json.has("password"));
    }

    @Test
    public void testFilter_DocumentField() throws ConfigException {
        doReturn(Optional.of("document")).when(this.configuration).getConfig(any(ApplicationContext.class), eq("services.data.filter.remove"));
        var factory = FilterFactory.of();
        var executors = factory.filters(this.context, this.configuration);
        var json = new JSONObject("{\"domain\":\"domain\",\"document\":\"abcd\",\"documentation\":\"12345\"}");
        executors.execute(this.context, json);

        assertFalse(json.has("document"));
    }
}
