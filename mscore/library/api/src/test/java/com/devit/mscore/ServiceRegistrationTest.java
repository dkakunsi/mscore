package com.devit.mscore;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThrows;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
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

  @Before
  public void setup() {
    this.configuration = mock(Configuration.class);
    doReturn("data").when(this.configuration).getServiceName();
    this.registry = mock(Registry.class);
    this.serviceRegistration = new ServiceRegistration(registry, configuration);
  }

  @Test
  public void testRegister_Service() throws RegistryException, ConfigException {
    doReturn(Optional.of("true")).when(this.configuration).getConfig("platform.service.registry.static");
    doReturn(Optional.of("static-address")).when(this.configuration).getConfig("services.data.registry.address");

    var service = mock(Service.class);
    doReturn("domain").when(service).getDomain();

    var spiedRegistration = spy(this.serviceRegistration);
    spiedRegistration.register(service);

    var keyCaptor = ArgumentCaptor.forClass(String.class);
    var valueCaptor = ArgumentCaptor.forClass(String.class);
    verify(this.registry, times(1)).add(keyCaptor.capture(), valueCaptor.capture());

    var key = keyCaptor.getValue();
    assertThat(key, is("/services/endpoint/domain/url"));
    var value = valueCaptor.getValue();
    assertThat(value, is("static-address/domain"));
  }

  @Test
  public void testRegister_StaticAddress() throws RegistryException, ConfigException {
    doReturn(Optional.of("true")).when(this.configuration).getConfig("platform.service.registry.static");
    doReturn(Optional.of("static-address")).when(this.configuration).getConfig("services.data.registry.address");

    var spiedRegistration = spy(this.serviceRegistration);
    spiedRegistration.register("domain");

    var keyCaptor = ArgumentCaptor.forClass(String.class);
    var valueCaptor = ArgumentCaptor.forClass(String.class);
    verify(this.registry, times(1)).add(keyCaptor.capture(), valueCaptor.capture());

    var key = keyCaptor.getValue();
    assertThat(key, is("/services/endpoint/domain/url"));
    var value = valueCaptor.getValue();
    assertThat(value, is("static-address/domain"));
  }

  @Test
  public void testRegister_DynamicAddress() throws RegistryException, UnknownHostException, ConfigException {
    doReturn(Optional.empty()).when(this.configuration).getConfig("platform.sewrvice.registry.static");
    doReturn(Optional.of("http")).when(this.configuration).getConfig("services.data.registry.protocol");
    doReturn(Optional.of("2000")).when(this.configuration).getConfig("platform.service.web.port");

    var currentAddress = Inet4Address.getLocalHost().getHostAddress();

    var spiedRegistration = spy(this.serviceRegistration);
    spiedRegistration.register("domain");

    var keyCaptor = ArgumentCaptor.forClass(String.class);
    var valueCaptor = ArgumentCaptor.forClass(String.class);
    verify(this.registry, times(1)).add(keyCaptor.capture(), valueCaptor.capture());

    var key = keyCaptor.getValue();
    assertThat(key, is("/services/endpoint/domain/url"));
    var value = valueCaptor.getValue();
    assertThat(value, is(String.format("http://%s:2000/domain", currentAddress)));
  }

  @Test
  public void testRegister_ThrowException() throws RegistryException, UnknownHostException, ConfigException {
    doThrow(new ConfigException("Error message.")).when(this.configuration)
        .getConfig("platform.service.registry.static");
    var ex = assertThrows(RegistryException.class, () -> this.serviceRegistration.register("domain"));

    var actualEx = ex.getCause();
    assertThat(actualEx, instanceOf(ConfigException.class));
    assertThat(actualEx.getMessage(), is("Error message."));
  }

  @Test
  public void testGet() throws RegistryException {
    var domain = "domain";
    this.serviceRegistration.get(domain);
    verify(this.registry).get(anyString());
  }

  @Test
  public void testOpenClose() {
    this.serviceRegistration.open();
    this.serviceRegistration.close();

    verify(this.registry).open();
    verify(this.registry).close();
  }
}
