package com.devit.mscore;

import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import com.devit.mscore.exception.ApplicationRuntimeException;
import com.devit.mscore.exception.RegistryException;
import com.devit.mscore.web.Client;

import org.junit.Test;

public class DataClientTest {
  @Test
  public void testCreate_NotSupportClone() throws CloneNotSupportedException {
    var client = mock(Client.class);
    var serviceRegistration = mock(ServiceRegistration.class);

    doReturn(client).when(client).clone();
    doThrow(CloneNotSupportedException.class).when(serviceRegistration).clone();

    assertThrows(ApplicationRuntimeException.class,
        () -> new DataClient(client, serviceRegistration, "workflowDomain"));
  }

  @Test
  public void testGetClient_NotSupportClone() throws CloneNotSupportedException {
    var client = mock(Client.class);
    var clonedClient = mock(Client.class);
    var serviceRegistration = mock(ServiceRegistration.class);

    doReturn(serviceRegistration).when(serviceRegistration).clone();
    doReturn(clonedClient).when(client).clone();
    doThrow(CloneNotSupportedException.class).when(clonedClient).clone();

    var dataClient = new DataClient(client, serviceRegistration, "workflowDomain");
    assertThrows(ApplicationRuntimeException.class, () -> dataClient.getClient());
  }

  @Test
  public void testGetUri_ThrowRegistryException() throws CloneNotSupportedException, RegistryException {
    var client = mock(Client.class);
    var serviceRegistration = mock(ServiceRegistration.class);

    doReturn(client).when(client).clone();
    doReturn(serviceRegistration).when(serviceRegistration).clone();
    doThrow(RegistryException.class).when(serviceRegistration).get(anyString());

    var dataClient = new DataClient(client, serviceRegistration, "workflowDomain");

    assertThrows(ApplicationRuntimeException.class, () -> dataClient.getWorkflowUri());
  }

  @Test
  public void testGetWorkflowUri_WhenNotCached() throws CloneNotSupportedException, RegistryException {
    var client = mock(Client.class);
    var serviceRegistration = mock(ServiceRegistration.class);

    doReturn(client).when(client).clone();
    doReturn(serviceRegistration).when(serviceRegistration).clone();
    doReturn("http://workflow").when(serviceRegistration).get("workflowDomain");

    var dataClient = new DataClient(client, serviceRegistration, "workflowDomain");

    dataClient.getWorkflowUri();
  }
}

