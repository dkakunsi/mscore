package com.devit.mscore.web.javalin;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.devit.mscore.ApplicationContext;
import com.devit.mscore.AuthenticationProvider;
import com.devit.mscore.Configuration;
import com.devit.mscore.DefaultApplicationContext;
import com.devit.mscore.Service;
import com.devit.mscore.Synchronizer;
import com.devit.mscore.exception.ApplicationException;
import com.devit.mscore.exception.DataDuplicationException;
import com.devit.mscore.exception.DataException;
import com.devit.mscore.exception.DataNotFoundException;
import com.devit.mscore.exception.ValidationException;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

public class JavalinWebTest {

    private static final String BASE_URL = "http://localhost:2220/domain";

    private static final String BASE_SECURE_URL = "http://localhost:2220/secure";

    private static JSONObject principal;

    private static Configuration configuration;

    private static Service service;

    private static Service secureService;

    private static Synchronizer synchronizer;

    private static AuthenticationProvider authentication;

    private static JavalinServer server;

    private static ApplicationContext context;

    @BeforeClass
    public static void setup() throws Exception {
        context = DefaultApplicationContext.of("test");

        configuration = mock(Configuration.class);
        doReturn(Optional.of("2220")).when(configuration).getConfig(any(ApplicationContext.class), eq("platform.service.web.port"));
        doReturn("test").when(configuration).getServiceName();

        principal = new JSONObject();
        var role = new JSONArray(List.of("user"));
        principal.put("role", role);

        authentication = mock(AuthenticationProvider.class);
        var roles = Map.of("/domain/*", "user", "/secure*", Map.of("POST", "admin"), "/secure/*", Map.of("PUT", "admin"), "/secure/*/sync", "admin", "/secure/sync", "admin");
        doReturn(roles).when(authentication).getUri();
        doReturn(principal).when(authentication).verify(any(JavalinApplicationContext.class), anyString());

        var apiFactory = JavalinApiFactory.of(context, configuration, authentication, null);

        service = mock(Service.class);
        doReturn("domain").when(service).getDomain();
        synchronizer = mock(Synchronizer.class);

        var controller = new JavalinController(service, synchronizer);
        var endpoint = new JavalinEndpoint(controller);

        apiFactory.add(endpoint);

        secureService = mock(Service.class);
        doReturn("secure").when(secureService).getDomain();
        var secureController = new JavalinController(secureService, synchronizer);
        apiFactory.add(secureController);

        var anotherService = mock(Service.class);
        doReturn("anotherService").when(anotherService).getDomain();
        var anotherEndpoint = new JavalinEndpoint(anotherService);

        apiFactory.add(anotherEndpoint);
        server = (JavalinServer) apiFactory.server(context);
        server.start();
    }

    @AfterClass
    public static void destroy() {
        server.stop();
    }

    @Deprecated
    @Test
    public void dummyTest() {
        var factory = JavalinApiFactory.of(context, configuration, authentication, null);
        assertNotNull(factory);
    }

    @Test
    public void testSave() throws ApplicationException {
        doReturn("id").when(service).save(any(ApplicationContext.class), any(JSONObject.class));

        var body = "{\"domain\":\"domain\",\"name\":\"name\"}";
        var response = Unirest.post(BASE_URL).body(body).asString();

        assertThat(response.getStatus(), is(200));

        var responseBody = response.getBody();
        assertNotNull(responseBody);

        var jsonResponse = new JSONObject(responseBody);
        assertThat(jsonResponse.getString("id"), is("id"));
    }

    @Test
    public void testPut() throws ApplicationException {
        doReturn("id").when(service).save(any(ApplicationContext.class), any(JSONObject.class));

        var body = "{\"domain\":\"domain\",\"name\":\"name\"}";
        var response = Unirest.put(BASE_URL + "/id").header("Authorization", "JWT Token").body(body).asString();

        assertThat(response.getStatus(), is(200));

        var responseBody = response.getBody();
        assertNotNull(responseBody);

        var jsonResponse = new JSONObject(responseBody);
        assertThat(jsonResponse.getString("id"), is("id"));
    }

    @Test
    public void testSave_WithoutDomain() throws ApplicationException {
        var body = "{\"name\":\"name\"}";
        Unirest.post(BASE_URL).body(body).asString();

        verify(service, atLeastOnce()).save(any(ApplicationContext.class), any(JSONObject.class));
    }

    @Test
    public void testSave_InvalidBody() {
        var body = "invalid body";
        var response = Unirest.post(BASE_URL).body(body).asString();
        assertErrorResponse(response, 400, "REQUEST ERROR", "Unexpected format.");
    }

    @Test
    public void testSave_ThrowValidationException() throws ApplicationException {
        var ex = new ValidationException("Validation message");
        doThrow(ex).when(service).save(any(ApplicationContext.class), any(JSONObject.class));

        var body = "{\"domain\":\"domain\",\"name\":\"name\"}";
        var response = Unirest.post(BASE_URL).body(body).asString();

        assertErrorResponse(response, 400, "REQUEST ERROR", "Validation message");
    }

    @Test
    public void testSave_ThrowDataException() throws ApplicationException {
        var ex = new DataException("Data message");
        doThrow(ex).when(service).save(any(ApplicationContext.class), any(JSONObject.class));

        var body = "{\"domain\":\"domain\",\"name\":\"name\"}";
        var response = Unirest.post(BASE_URL).body(body).asString();

        assertErrorResponse(response, 500, "SERVER ERROR", "Data message");
    }

    @Test
    public void testSave_ThrowDataNotFoundException() throws ApplicationException {
        var ex = new DataNotFoundException("Data not found message");
        doThrow(ex).when(service).save(any(ApplicationContext.class), any(JSONObject.class));

        var body = "{\"domain\":\"domain\",\"name\":\"name\"}";
        var response = Unirest.post(BASE_URL).body(body).asString();

        assertErrorResponse(response, 404, "REQUEST ERROR", "Data not found message");
    }

    @Test
    public void testSave_ThrowDataDuplicationException() throws ApplicationException {
        var ex = new DataDuplicationException("Data duplication message");
        doThrow(ex).when(service).save(any(ApplicationContext.class), any(JSONObject.class));

        var body = "{\"domain\":\"domain\",\"name\":\"name\"}";
        var response = Unirest.post(BASE_URL).body(body).asString();

        assertErrorResponse(response, 400, "REQUEST ERROR", "Data duplication message");
    }

    @Test
    public void testGetOne() throws ApplicationException {
        var json = new JSONObject("{\"domain\":\"domain\",\"id\":\"id\",\"name\":\"name\"}");
        doReturn(json).when(service).find(any(ApplicationContext.class), eq("id"));

        var response = Unirest.get(BASE_URL + "/id").header("Authorization", "JWT Token").asString();

        assertThat(response.getStatus(), is(200));

        var responseBody = response.getBody();
        assertNotNull(responseBody);

        var jsonResponse = new JSONObject(responseBody);
        assertThat(jsonResponse.getString("id"), is("id"));
        assertThat(jsonResponse.getString("domain"), is("domain"));
        assertThat(jsonResponse.getString("name"), is("name"));
    }

    @Test
    public void testGetOneByCode() throws ApplicationException {
        var json = new JSONObject("{\"domain\":\"domain\",\"id\":\"id\",\"name\":\"name\"}");
        doReturn(json).when(service).findByCode(any(ApplicationContext.class), eq("code"));

        var response = Unirest.get(BASE_URL + "/code/code").header("Authorization", "JWT Token").asString();

        assertThat(response.getStatus(), is(200));

        var responseBody = response.getBody();
        assertNotNull(responseBody);

        var jsonResponse = new JSONObject(responseBody);
        assertThat(jsonResponse.getString("id"), is("id"));
        assertThat(jsonResponse.getString("domain"), is("domain"));
        assertThat(jsonResponse.getString("name"), is("name"));
    }

    @Test
    public void testGetMany() throws ApplicationException {
        var json = new JSONArray("[{\"domain\":\"domain\",\"id\":\"id\",\"name\":\"name\"}]");
        doReturn(json).when(service).find(any(ApplicationContext.class), anyList());

        var response = Unirest.get(BASE_URL + "/keys").header("Authorization", "JWT Token").queryString(Map.of("ids", "id1")).asString();

        assertThat(response.getStatus(), is(200));

        var responseBody = response.getBody();
        assertNotNull(responseBody);

        var jsonResponse = new JSONArray(responseBody);
        assertThat(jsonResponse.length(), is(1));

        var jsonObject = jsonResponse.getJSONObject(0);
        assertThat(jsonObject.getString("id"), is("id"));
        assertThat(jsonObject.getString("domain"), is("domain"));
        assertThat(jsonObject.getString("name"), is("name"));
    }

    @Test
    public void testGet_NotFound() throws ApplicationException {
        var json = new JSONObject();
        doReturn(json).when(service).find(any(ApplicationContext.class), eq("id"));

        var response = Unirest.get(BASE_URL + "/id").header("Authorization", "JWT Token").asString();

        assertThat(response.getStatus(), is(200));

        var responseBody = response.getBody();
        assertNotNull(responseBody);

        var jsonResponse = new JSONObject(responseBody);
        assertTrue(jsonResponse.isEmpty());
    }

    @Test
    public void testDelete() throws ApplicationException {
        var response = Unirest.delete(BASE_URL + "/id").header("Authorization", "JWT Token").asString();

        assertThat(response.getStatus(), is(501));

        var responseBody = response.getBody();
        assertNotNull(responseBody);

        var jsonResponse = new JSONObject(responseBody);
        assertThat(jsonResponse.getString("message"), is("Delete is not supported."));
        assertThat(jsonResponse.getString("type"), is("SERVER ERROR"));
    }

    @Test
    public void testGetMany_WithoutParam() throws ApplicationException {
        var response = Unirest.get(BASE_URL + "/keys").header("Authorization", "JWT Token").asString();
        assertErrorResponse(response, 400, "REQUEST ERROR", "List of IDs is not provided");
    }

    @Test
    public void testSearch() throws ApplicationException {
        doReturn(new JSONArray("[{\"domain\":\"domain\",\"id\":\"id\",\"name\":\"Family Name\"}]")).when(service).search(any(ApplicationContext.class), any(JSONObject.class));

        var body = "{\"criteria\": [{\"attribute\": \"name\",\"value\": \"Family\",\"operator\": \"contains\"}]}";
        var response = Unirest.post(BASE_URL + "/search").header("Authorization", "JWT Token").body(body).asString();

        assertThat(response.getStatus(), is(200));

        var responseBody = response.getBody();
        assertNotNull(responseBody);

        var jsonResponse = new JSONArray(responseBody);
        assertThat(jsonResponse.length(), is(1));

        var jsonObject = jsonResponse.getJSONObject(0);
        assertThat(jsonObject.getString("domain"), is("domain"));
        assertThat(jsonObject.getString("id"), is("id"));
        assertThat(jsonObject.getString("name"), is("Family Name"));
    }

    @Test
    public void testSearch_NotFound() throws ApplicationException {
        doReturn(new JSONArray()).when(service).search(any(ApplicationContext.class), any(JSONObject.class));

        var body = "{\"criteria\": [{\"attribute\": \"name\",\"value\": \"Family\",\"operator\": \"contains\"}]}";
        var response = Unirest.post(BASE_URL + "/search").header("Authorization", "JWT Token").body(body).asString();

        assertThat(response.getStatus(), is(200));

        var responseBody = response.getBody();
        assertNotNull(responseBody);

        var jsonResponse = new JSONArray(responseBody);
        assertTrue(jsonResponse.isEmpty());
    }

    @Test
    public void testSyncById() {
        var response = Unirest.post(BASE_URL + "/id/sync").header("Authorization", "JWT Token").asString();

        assertThat(response.getStatus(), is(200));

        var responseBody = response.getBody();
        assertNotNull(responseBody);

        var jsonResponse = new JSONObject(responseBody);
        assertThat(jsonResponse.getString("message"), is("Synchronization process is in progress."));
    }

    @Test
    public void testSyncAll() {
        var response = Unirest.post(BASE_URL + "/sync").header("Authorization", "JWT Token").asString();

        assertThat(response.getStatus(), is(200));

        var responseBody = response.getBody();
        assertNotNull(responseBody);

        var jsonResponse = new JSONObject(responseBody);
        assertThat(jsonResponse.getString("message"), is("Synchronization process is in progress."));
    }

    private void assertErrorResponse(HttpResponse<String> response, int status, String errorType, String message) {
        assertThat(response.getStatus(), is(status));

        var responseBody = response.getBody();
        assertNotNull(responseBody);

        var jsonResponse = new JSONObject(responseBody);
        assertThat(jsonResponse.getString("message"), is(message));
        assertThat(jsonResponse.getString("type"), is(errorType));
    }

    
    @Test
    public void testSecureSave() throws ApplicationException {
        doReturn("id").when(secureService).save(any(ApplicationContext.class), any(JSONObject.class));

        var body = "{\"domain\":\"secure\",\"name\":\"name\"}";
        var response = Unirest.post(BASE_SECURE_URL).header("Authorization", "JWT Token").body(body).asString();

        assertThat(response.getStatus(), is(403));
    }

    @Test
    public void testSecureSave_WithoutToken() throws ApplicationException {
        doReturn("id").when(secureService).save(any(ApplicationContext.class), any(JSONObject.class));

        var body = "{\"domain\":\"secure\",\"name\":\"name\"}";
        var response = Unirest.post(BASE_SECURE_URL).body(body).asString();

        assertThat(response.getStatus(), is(401));
    }

    @Test
    public void testSecurePut() throws ApplicationException {
        doReturn("id").when(secureService).save(any(ApplicationContext.class), any(JSONObject.class));

        var body = "{\"domain\":\"secure\",\"name\":\"name\"}";
        var response = Unirest.put(BASE_SECURE_URL + "/id").header("Authorization", "JWT Token").body(body).asString();

        assertThat(response.getStatus(), is(403));
    }

    @Test
    public void testSecure_GetOne() throws ApplicationException {
        var json = new JSONObject("{\"domain\":\"secure\",\"id\":\"id\",\"name\":\"name\"}");
        doReturn(json).when(secureService).find(any(ApplicationContext.class), eq("id"));

        var response = Unirest.get(BASE_SECURE_URL + "/id").header("Authorization", "JWT Token").asString();

        assertThat(response.getStatus(), is(200));

        var responseBody = response.getBody();
        assertNotNull(responseBody);

        var jsonResponse = new JSONObject(responseBody);
        assertThat(jsonResponse.getString("id"), is("id"));
        assertThat(jsonResponse.getString("domain"), is("secure"));
        assertThat(jsonResponse.getString("name"), is("name"));
    }

    @Test
    public void testSecure_GetOneByCode() throws ApplicationException {
        var json = new JSONObject("{\"domain\":\"secure\",\"id\":\"id\",\"name\":\"name\"}");
        doReturn(json).when(secureService).findByCode(any(ApplicationContext.class), eq("code"));

        var response = Unirest.get(BASE_SECURE_URL + "/code/code").header("Authorization", "JWT Token").asString();

        assertThat(response.getStatus(), is(200));

        var responseBody = response.getBody();
        assertNotNull(responseBody);

        var jsonResponse = new JSONObject(responseBody);
        assertThat(jsonResponse.getString("id"), is("id"));
        assertThat(jsonResponse.getString("domain"), is("secure"));
        assertThat(jsonResponse.getString("name"), is("name"));
    }

    @Test
    public void testSecure_GetMany() throws ApplicationException {
        var json = new JSONArray("[{\"domain\":\"secure\",\"id\":\"id\",\"name\":\"name\"}]");
        doReturn(json).when(secureService).find(any(ApplicationContext.class), anyList());

        var response = Unirest.get(BASE_SECURE_URL + "/keys").header("Authorization", "JWT Token").queryString(Map.of("ids", "id1")).asString();

        assertThat(response.getStatus(), is(200));

        var responseBody = response.getBody();
        assertNotNull(responseBody);

        var jsonResponse = new JSONArray(responseBody);
        assertThat(jsonResponse.length(), is(1));

        var jsonObject = jsonResponse.getJSONObject(0);
        assertThat(jsonObject.getString("id"), is("id"));
        assertThat(jsonObject.getString("domain"), is("secure"));
        assertThat(jsonObject.getString("name"), is("name"));
    }

    @Test
    public void testSecure_Delete() throws ApplicationException {
        var response = Unirest.delete(BASE_SECURE_URL + "/id").header("Authorization", "JWT Token").asString();

        assertThat(response.getStatus(), is(501));

        var responseBody = response.getBody();
        assertNotNull(responseBody);

        var jsonResponse = new JSONObject(responseBody);
        assertThat(jsonResponse.getString("message"), is("Delete is not supported."));
        assertThat(jsonResponse.getString("type"), is("SERVER ERROR"));
    }

    @Test
    public void testSecure_SyncById() {
        var response = Unirest.get(BASE_SECURE_URL + "/id/sync").header("Authorization", "JWT Token").asString();
        assertThat(response.getStatus(), is(403));
    }

    @Test
    public void testSecure_SyncAll() {
        var response = Unirest.get(BASE_SECURE_URL + "/sync").header("Authorization", "JWT Token").asString();
        assertThat(response.getStatus(), is(403));
    }
}
