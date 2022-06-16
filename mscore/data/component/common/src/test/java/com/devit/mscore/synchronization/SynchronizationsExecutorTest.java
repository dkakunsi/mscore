package com.devit.mscore.synchronization;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import com.devit.mscore.ApplicationContext;
import com.devit.mscore.Schema;
import com.devit.mscore.Synchronizer;
import com.devit.mscore.exception.ResourceException;
import com.devit.mscore.exception.SynchronizationException;

import org.hamcrest.core.Is;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

public class SynchronizationsExecutorTest {

    private SynchronizationsExecutor executor;

    private Synchronizer synchronizer;

    private ApplicationContext context;

    @Before
    public void setup() {
        this.executor = new SynchronizationsExecutor();
        this.synchronizer = mock(Synchronizer.class);
        this.context = mock(ApplicationContext.class);
        doReturn("breadcrumbId").when(this.context).getBreadcrumbId();
    }

    @Test
    public void testSynchronize() throws SynchronizationException {
        var synchronization = new DefaultSynchronization(synchronizer, "domain", "attribute");
        this.executor.add(synchronization);

        var json = "{\"domain\":\"domain\",\"id\":\"id\"}";
        this.executor.execute(this.context, new JSONObject(json));

        verify(this.synchronizer, times(1)).synchronize(any(ApplicationContext.class), anyString(), anyString());
    }

    @Test
    public void testSynchronize_WithoutDomain() throws SynchronizationException {
        var synchronization = new DefaultSynchronization(synchronizer, "domain", "attribute");
        this.executor.add(synchronization);

        var json = "{\"id\":\"id\"}";
        this.executor.execute(this.context, new JSONObject(json));

        verify(this.synchronizer, times(0)).synchronize(any(ApplicationContext.class), anyString(), anyString());
    }

    @Test
    public void testSynchronize_ThrowException() throws SynchronizationException {
        doThrow(new SynchronizationException("")).when(this.synchronizer).synchronize(any(ApplicationContext.class), anyString(), anyString());
        var synchronization = new DefaultSynchronization(synchronizer, "domain", "attribute");
        this.executor.add(synchronization);

        var json = "{\"domain\":\"domain\",\"id\":\"id\"}";
        this.executor.execute(this.context, new JSONObject(json));

        verify(this.synchronizer, times(1)).synchronize(any(ApplicationContext.class), anyString(), anyString());
    }

    @Test
    public void testAdd_Synchronizer() throws ResourceException, URISyntaxException {
        var schema = mock(Schema.class);
        doReturn(Map.of("referenceAttribute", List.of("referenceDomain"))).when(schema).getReferences();
        doReturn(schema).when(this.synchronizer).getSchema();

        var initialNumberOfSynhronization = this.executor.getSynchronizations().size();
        this.executor.add(synchronizer);
        assertThat(this.executor.getSynchronizations().size(), Is.is(initialNumberOfSynhronization + 1));

        var synchronization = this.executor.getSynchronizations().get("referenceDomain");
        assertThat(synchronization.get(0).getReferenceAttribute(), Is.is("referenceAttribute"));
    }
    
    public static File getResourceFile(String resourceName) throws URISyntaxException {
        var resource = SynchronizationsExecutorTest.class.getClassLoader().getResource(resourceName);
        return new File(resource.toURI());
    }
}
