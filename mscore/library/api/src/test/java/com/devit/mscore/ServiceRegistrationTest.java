package com.devit.mscore;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThrows;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.Optional;

import com.devit.mscore.exception.ConfigException;
import com.devit.mscore.exception.RegistryException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class ServiceRegistrationTest {

    private Configuration configuration;

    private Registry registry;

    private ServiceRegistration serviceRegistration;

    private ApplicationContext context;

    @Before
    public void setup() {
        this.configuration = mock(Configuration.class);
        doReturn("data").when(this.configuration).getServiceName();
        this.registry = mock(Registry.class);
        this.serviceRegistration = new ServiceRegistration(registry, configuration);
        this.context = DefaultApplicationContext.of("test");
    }

    @Test
    public void testRegister_Service() throws RegistryException, ConfigException {
        doReturn(Optional.of("true")).when(this.configuration).getConfig(any(ApplicationContext.class), eq("platform.service.registry.static"));
        doReturn(Optional.of("static-address")).when(this.configuration).getConfig(any(ApplicationContext.class), eq("services.data.registry.address"));

        var service = mock(Service.class);
        doReturn("domain").when(service).getDomain();

        var spiedRegistration = spy(this.serviceRegistration);
        spiedRegistration.register(this.context, service);

        var keyCaptor = ArgumentCaptor.forClass(String.class);
        var valueCaptor = ArgumentCaptor.forClass(String.class);
        verify(this.registry, times(1)).add(any(ApplicationContext.class), keyCaptor.capture(), valueCaptor.capture());

        var key = keyCaptor.getValue();
        assertThat(key, is("/services/endpoint/domain/url"));
        var value = valueCaptor.getValue();
        assertThat(value, is("static-address/domain"));
    }

    @Test
    public void testRegister_StaticAddress() throws RegistryException, ConfigException {
        doReturn(Optional.of("true")).when(this.configuration).getConfig(any(ApplicationContext.class), eq("platform.service.registry.static"));
        doReturn(Optional.of("static-address")).when(this.configuration).getConfig(any(ApplicationContext.class), eq("services.data.registry.address"));

        var spiedRegistration = spy(this.serviceRegistration);
        spiedRegistration.register(this.context, "domain");

        var keyCaptor = ArgumentCaptor.forClass(String.class);
        var valueCaptor = ArgumentCaptor.forClass(String.class);
        verify(this.registry, times(1)).add(any(ApplicationContext.class), keyCaptor.capture(), valueCaptor.capture());

        var key = keyCaptor.getValue();
        assertThat(key, is("/services/endpoint/domain/url"));
        var value = valueCaptor.getValue();
        assertThat(value, is("static-address/domain"));
    }

    @Test
    public void testRegister_DynamicAddress() throws RegistryException, UnknownHostException, ConfigException {
        doReturn(Optional.empty()).when(this.configuration).getConfig(any(ApplicationContext.class), eq("platform.sewrvice.registry.static"));
        doReturn(Optional.of("http")).when(this.configuration).getConfig(any(ApplicationContext.class), eq("services.data.registry.protocol"));
        doReturn(Optional.of("2000")).when(this.configuration).getConfig(any(ApplicationContext.class), eq("platform.service.web.port"));

        var currentAddress = Inet4Address.getLocalHost().getHostAddress();

        var spiedRegistration = spy(this.serviceRegistration);
        spiedRegistration.register(this.context, "domain");

        var keyCaptor = ArgumentCaptor.forClass(String.class);
        var valueCaptor = ArgumentCaptor.forClass(String.class);
        verify(this.registry, times(1)).add(any(ApplicationContext.class), keyCaptor.capture(), valueCaptor.capture());

        var key = keyCaptor.getValue();
        assertThat(key, is("/services/endpoint/domain/url"));
        var value = valueCaptor.getValue();
        assertThat(value, is(String.format("http://%s:2000/domain", currentAddress)));
    }

    @Test
    public void testRegister_ThrowException() throws RegistryException, UnknownHostException, ConfigException {
        doThrow(new ConfigException("Error message.")).when(this.configuration).getConfig(any(ApplicationContext.class), eq("platform.service.registry.static"));
        var ex = assertThrows(RegistryException.class, () -> this.serviceRegistration.register(this.context, "domain"));

        var actualEx = ex.getCause();
        assertThat(actualEx, instanceOf(ConfigException.class));
        assertThat(actualEx.getMessage(), is("Error message."));
    }

    @Test
    public void testGet() throws RegistryException {
        var domain = "domain";
        this.serviceRegistration.get(this.context, domain);
        verify(this.registry).get(any(ApplicationContext.class), anyString());
    }

    @Test
    public void testOpenClose() {
        this.serviceRegistration.open();
        this.serviceRegistration.close();

        verify(this.registry).open();
        verify(this.registry).close();
    }
}
