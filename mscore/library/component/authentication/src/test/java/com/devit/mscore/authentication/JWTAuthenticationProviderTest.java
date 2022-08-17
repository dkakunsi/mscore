package com.devit.mscore.authentication;

import static com.devit.mscore.util.Utils.REQUESTED_BY;
import static com.devit.mscore.util.Utils.ROLE;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.devit.mscore.Configuration;
import com.devit.mscore.exception.ApplicationException;
import com.devit.mscore.exception.AuthenticationException;
import com.devit.mscore.exception.ConfigException;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

public class JWTAuthenticationProviderTest {

  private static final String PRIVATE_KEY = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQC1LyFb6kD7y+5YUUvbn3Hy1t3LXgy8lOmH3dcuvozwn+AC5Y87v5rLpXAboSNFjVCjHZLNnh/6IkoWi9RvaeqAH5N97NlV2B9+7jxEjsZamoSDWZ4Ln7436XlCBDg0eGR8S4f1iLD2fNEWJLg3TPXgiHl8OUHILMDpJfsuTGO6uQ6bpb8AiN26UJmHn/8z2kkZ4tZzNDQbHz7cAhM82m6Tul11IPUqnqoq83pb0nyl+soXxUZWS4mZOy1GudHQoILuk6lR94hBaiKfwJoxqZrZC0hJ42GQY1oYXpst4HGuDVUzN1rdjOn0ScZAwSUlhDinr4CskxpETOkRFdCBNB0bAgMBAAECggEAPfw6yGxTARHan+JoNmHNJQw3YvzxFI9Jec7+cCKGq0e25qbMot9BQQx/VySAoanf/X3/nFLNk9CpUh9SdS9iJ6Ul233tOL4wwEcW3UmPOK4GSb2eIVHsTMqWTmyNIf5SOmfIwsqZ6Cn5ij7Tuy+cKs3l6gbYp3gQI3N4BHXj5JwZoFAZVZgMfYlMpzbrOFSUrSqAR80gTOmTbgo4ZTw3QU5lToMzQ/KJPMJsjxBkTal3dZ6HU9rGBTzNVpGOD0BvTEKTSof+jIofbj59IoXW04O5rWZoE9gBF7DdvYc6+ie8/djJAYkJBPNEmC1s3Utp7/5nmckHVpvkKV1tlup70QKBgQDeYIC6+EBZ9uhC1Iqec8Hgmtg2eL1ldPlpd7E8IB5gNBFuxJsAQs2bQkZOBe1ZU7h3JlkAH+isCfNuOtH16G54CBSqONjvP2bsGWJqYzpG/psZvjsFoPCmvB1rlDPXsLxDP7ps1tjkqrYlY45A8rblEPPJpkg2F0XRbFYMGjD1QwKBgQDQlC8rllziGjs/UkDi7x3MHrrSAg1ldKzy3aJ2LkbhYZ3IE0sfeDtp3yV6yDkFVl9EU8PiWO99Rc7tYEAuMqJjW6rtF0cqGZsJZbPLKGKXt4/On2MDUYPin1XHYomlSe8OtpOEGVYxlsIXxqvOXuUJ7W/HgoMCO1rA8dvPXA/PSQKBgQDSqDJqa+9yCf7eCD/EeL4JykXV3Cz2pof6zCL+ZSLBWbHF78MxzRa+5Fp7YQwF2dReMtqOzqt4Bfkvy9LIE8ZKOMVyt2Vxxur179oWFCfJxzkget+oplwyZvOrzHoL8mV1gzJUFnbir4DbDGNezU5K0vNObBHuA7/k8q7Uyh7kxwKBgFfieFWnT4+9ecVehRSZqDZ/pDwkvTxIgy76ECA3s4n3taG972NdJ7ueWI55mv0SvaVunhTbYF2qclw2uBQ/JYkz8LthmYy1qUu2XKF3bMN8hs2K/w9A448zj9MpQ9IvatkKOPHqMxVF7pZSEcYs2djrALRR252vILg3sGSY59hxAoGBANVNJ5P+64vpCc6lxLszuVfBtUvBgvFtZQoGMlKCXEahVlmztqZyQWhZFZiI3kxSZqNZ8f3fk3Bu1m9yJOWpDA/QJieGHYehnom5rTn7rCEojU7bu74Zpk+fOPylD2/41GGpCcw0cBvNqmxUf400vfLtVbd2qSANy3+qXpsvosAW";

  private static final String PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAtS8hW+pA+8vuWFFL259x8tbdy14MvJTph93XLr6M8J/gAuWPO7+ay6VwG6EjRY1Qox2SzZ4f+iJKFovUb2nqgB+TfezZVdgffu48RI7GWpqEg1meC5++N+l5QgQ4NHhkfEuH9Yiw9nzRFiS4N0z14Ih5fDlByCzA6SX7LkxjurkOm6W/AIjdulCZh5//M9pJGeLWczQ0Gx8+3AITPNpuk7pddSD1Kp6qKvN6W9J8pfrKF8VGVkuJmTstRrnR0KCC7pOpUfeIQWoin8CaMama2QtISeNhkGNaGF6bLeBxrg1VMzda3Yzp9EnGQMElJYQ4p6+ArJMaREzpERXQgTQdGwIDAQAB";

  private static final String KEYCLOAK_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAm871bzTG2qgBExsxTRBXMnf71ARlNroWot33WsXak8sr46rBdInsgwdjRi2bwkK+Z23QIsHtWfF+7+Iyn0fwOYwDhLMrphmWT33VhcVKBFuILyC573C5+HjBqsB5i8dzqElLE5+NnqPdQ0iag0ByHyAAAL3/F5MmGnAKhH7e8Y8joYwj5TjNilkta6HKukVRgHd7oTNhI2N1S/2IvAlQ/JMBfempdQk0uPxvRfwJu8eT68/BvOoC0m3frfikATsAZzg+Ztj2K5Ni7rAPnGky6n6icVZiSjw4kasODsCyf96Lhx+15vJMF2kM0IovmqOkNA7L88fPfhEl4D82s8eq1QIDAQAB";

  private static final String SECURITY_CONFIG = "{\"domain\":\"user\"}";

  private JWTAuthenticationProvider provider;

  private Configuration configuration;

  @Before
  public void setup() throws ApplicationException {
    this.configuration = mock(Configuration.class);
    doReturn(Optional.of(KEYCLOAK_PUBLIC_KEY)).when(this.configuration).getConfig("platform.keycloak.public.key");
    doReturn("data").when(this.configuration).getServiceName();
    doReturn(true).when(this.configuration).has("services.data.secure.uri");
    doReturn(Optional.of(SECURITY_CONFIG)).when(this.configuration).getConfig("services.data.secure.uri");
    this.provider = JWTAuthenticationProvider.of(this.configuration);

    try {
      this.provider.storeToken(new JSONObject());
    } catch (ApplicationException ex) {
    }
  }

  @Test
  public void testInvalidSecurityConfig() throws ApplicationException {
    var configuration = mock(Configuration.class);
    doReturn(true).when(configuration).has("services.data.secure.uri");
    doReturn("data").when(configuration).getServiceName();
    doReturn(Optional.of("invalid")).when(configuration).getConfig("services.data.secure.uri");
    doReturn(Optional.of(PUBLIC_KEY)).when(configuration).getConfig("platform.keycloak.public.key");

    var ex = assertThrows(ConfigException.class, () -> JWTAuthenticationProvider.of(configuration));
    assertThat(ex.getMessage(), is("Invalid security configuration."));
    assertThat(ex.getCause(), instanceOf(JSONException.class));
  }

  @Test
  public void testNoSecurityConfig() throws ApplicationException {
    var configuration = mock(Configuration.class);
    doReturn(false).when(configuration).has("services.data.secure.uri");
    doReturn(Optional.of(PUBLIC_KEY)).when(configuration).getConfig("platform.keycloak.public.key");
    var auth = JWTAuthenticationProvider.of(configuration);
    assertTrue(auth.getUri().isEmpty());
  }

  @Test
  public void testEmptySecurityConfig() throws ApplicationException {
    var configuration = mock(Configuration.class);
    doReturn(true).when(configuration).has("services.data.secure.uri");
    doReturn(Optional.empty()).when(configuration).getConfig("services.data.secure.uri");
    doReturn(Optional.of(PUBLIC_KEY)).when(configuration).getConfig("platform.keycloak.public.key");
    var auth = JWTAuthenticationProvider.of(configuration);
    assertTrue(auth.getUri().isEmpty());
  }

  @Test
  public void testInvalidKey() throws ApplicationException {
    doReturn(Optional.of("asd")).when(this.configuration).getConfig("platform.keycloak.public.key");

    var ex = assertThrows(ApplicationException.class, () -> JWTAuthenticationProvider.of(this.configuration));
    assertThat(ex.getMessage(), is("Cannot create RSA key for authentication."));
    assertThat(ex.getCause(), instanceOf(InvalidKeySpecException.class));
  }

  @Test
  public void testKeyNotProvided() throws ApplicationException {
    doReturn(Optional.empty()).when(this.configuration).getConfig("platform.keycloak.public.key");

    var ex = assertThrows(ApplicationException.class, () -> JWTAuthenticationProvider.of(this.configuration));
    assertThat(ex.getMessage(), is("Public key is not configured correctly."));
    assertThat(ex, instanceOf(ConfigException.class));
  }

  @Test
  public void testVerify_ExpireToken() throws AuthenticationException {
    var token = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJzOHFsNE5LanJ6TDl3YS1fcHpVNzdSQ1c0ZHA1YjBKVWdJb0g4dEpCT0drIn0.eyJleHAiOjE2MjM1NjE4NzgsImlhdCI6MTYyMzU2MTU3OCwianRpIjoiOTE5ZmEzYWMtNzZjMC00OTk2LTk3YjUtNzkxNzBiNDI4NzQ4IiwiaXNzIjoiaHR0cDovL2xvY2FsaG9zdDoxMDEwL2F1dGgvcmVhbG1zL2FkdmVydG9yaWFsIiwiYXVkIjoiYWNjb3VudCIsInN1YiI6IjEzOGEwNGQxLThlZTgtNDk5Yy1hZmJiLTNiMWI4OTI1MDgxZiIsInR5cCI6IkJlYXJlciIsImF6cCI6IkdBVEVXQVkiLCJzZXNzaW9uX3N0YXRlIjoiMDNlZjEzNjktMDJhZi00ZWY0LWJhZDQtNTIzZjM2ZGQ1OTQ3IiwiYWNyIjoiMSIsInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6WyJkZWZhdWx0LXJvbGVzLWFkdmVydG9yaWFsIiwib2ZmbGluZV9hY2Nlc3MiLCJBRFZFUlRPUklBTCIsInVtYV9hdXRob3JpemF0aW9uIiwiVVNFUiJdfSwicmVzb3VyY2VfYWNjZXNzIjp7ImFjY291bnQiOnsicm9sZXMiOlsibWFuYWdlLWFjY291bnQiLCJtYW5hZ2UtYWNjb3VudC1saW5rcyIsInZpZXctcHJvZmlsZSJdfX0sInNjb3BlIjoicHJvZmlsZSBlbWFpbCIsImVtYWlsX3ZlcmlmaWVkIjpmYWxzZSwibmFtZSI6IkRlZGR5IEtha3Vuc2kiLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiJka2FrdW5zaSIsImdpdmVuX25hbWUiOiJEZWRkeSIsImZhbWlseV9uYW1lIjoiS2FrdW5zaSIsImVtYWlsIjoiZGVkZHkua2FrdW5zaUBnbWFpbC5jb20ifQ.YkspSZOY7NqS85OeCWf5V2G1FkihdGVRUGkCuBu1JvlkqY2KaMOeNUK2Gkt47QA7XyMeaUgo59kzn99cCgZyVOUnN8GoWA2P6PBUH4Erg6Siqlo705S3A3_he0oCOUxsuAQRS_7wupYtqKQzElVrb9fykoySvVER0AvLhPGsOstpQXalPucyG40rXPxZ7mYJ-9rhJuwJMK8IwpPQbKwpsaa1eoGCTSsHFOjRV5OiBXg7r4GO1YJOa7rmuT6hSE0L0AGPQhQnZcAE1R2s19OvU6x7Nc85m2XBVxjRfJQlBQfiUNDUbzjq9MzN-kbkCHqmE5iC9nmGoRhVX1TR9d4EhQ";

    var ex = assertThrows(AuthenticationException.class, () -> this.provider.verify(token));
    assertThat(ex.getMessage(), is("Token is not valid."));
    assertThat(ex.getCause(), instanceOf(TokenExpiredException.class));
  }

  @Test
  public void testVerify() throws ApplicationException, NoSuchAlgorithmException, InvalidKeySpecException {
    var token = createToken();
    doReturn(Optional.of(PUBLIC_KEY)).when(this.configuration).getConfig("platform.keycloak.public.key");
    provider = JWTAuthenticationProvider.of(this.configuration);
    var principal = provider.verify(token);
    assertNotNull(principal);
    assertEquals("Deddy Kakunsi", principal.getString(REQUESTED_BY));
    assertTrue(principal.getJSONArray(ROLE).toList().contains("USER"));
  }

  private static String createToken() throws NoSuchAlgorithmException, InvalidKeySpecException {
    var privateKey = privateKey();
    var algorithm = Algorithm.RSA256(null, privateKey);
    return JWT.create().withPayload(createPayload()).sign(algorithm);
  }

  private static RSAPrivateKey privateKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
    var decodedPrivateKey = Base64.getDecoder().decode(PRIVATE_KEY.getBytes(StandardCharsets.UTF_8));
    var keySpec = new PKCS8EncodedKeySpec(decodedPrivateKey);
    var keyFactory = KeyFactory.getInstance("RSA");
    return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
  }

  private static Map<String, Object> createPayload() {
    var realmAccess = new HashMap<>();
    realmAccess.put("roles", List.of("USER"));

    var map = new HashMap<String, Object>();
    map.put("realm_access", realmAccess);
    map.put("name", "Deddy Kakunsi");

    return map;
  }
}
