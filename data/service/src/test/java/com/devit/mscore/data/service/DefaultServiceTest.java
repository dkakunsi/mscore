package com.devit.mscore.data.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.devit.mscore.ApplicationContext;
import com.devit.mscore.DefaultApplicationContext;
import com.devit.mscore.FiltersExecutor;
import com.devit.mscore.Index;
import com.devit.mscore.Publisher;
import com.devit.mscore.Repository;
import com.devit.mscore.Schema;
import com.devit.mscore.data.enrichment.EnrichmentsExecutor;
import com.devit.mscore.data.observer.IndexingObserver;
import com.devit.mscore.data.observer.PublishingObserver;
import com.devit.mscore.data.validation.ValidationsExecutor;
import com.devit.mscore.exception.ApplicationException;
import com.devit.mscore.exception.ValidationException;

import java.util.List;
import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

public class DefaultServiceTest {

  private static final String PUBLISHING_CHANNEL = "channel1";

  private DefaultService service;

  private Repository repository;

  private Index index;

  private Publisher publisher;

  private Schema schema;

  @Before
  public void setup() throws CloneNotSupportedException {
    this.repository = mock(Repository.class);
    this.index = mock(Index.class);
    this.publisher = mock(Publisher.class);
    this.schema = mock(Schema.class);
    var validator = mock(ValidationsExecutor.class);
    var filter = mock(FiltersExecutor.class);
    var enricher = mock(EnrichmentsExecutor.class);
    var indexingObserver = new IndexingObserver(this.index, enricher);
    var publishingObserver = new PublishingObserver(this.publisher,PUBLISHING_CHANNEL);
    this.service = new DefaultService(this.schema, this.repository, this.index, validator, filter)
        .addObserver(indexingObserver).addObserver(publishingObserver);

    doReturn("domain").when(schema).getDomain();
    assertThat(this.service.getDomain(), is("domain"));
  }

  @Test
  public void testSave() throws ApplicationException {
    var context = DefaultApplicationContext.of("test");
    try (MockedStatic<ApplicationContext> utilities = Mockito.mockStatic(ApplicationContext.class)) {
      utilities.when(() -> ApplicationContext.getContext()).thenReturn(context);

      var result = new JSONObject("{\"domain\":\"domain\",\"id\":\"newId\"}");
      doReturn(result).when(this.repository).save(any(JSONObject.class));
      var json = new JSONObject("{\"domain\":\"domain\"}");
      var id = this.service.save(json);

      assertThat(id, is("newId"));
      assertThat(json.getString("domain"), is("domain"));
    }
  }

  @Test
  public void testSave_WithDependencies() throws ApplicationException {
    var context = DefaultApplicationContext.of("test");
    try (MockedStatic<ApplicationContext> utilities = Mockito.mockStatic(ApplicationContext.class)) {
      utilities.when(() -> ApplicationContext.getContext()).thenReturn(context);

      var result = new JSONObject("{\"domain\":\"domain\",\"id\":\"newId\"}");
      doReturn(result).when(this.repository).save(any(JSONObject.class));
      var json = new JSONObject("{\"domain\":\"domain\"}");
      var id = this.service.save(json);

      assertThat(id, is("newId"));
      assertThat(json.getString("domain"), is("domain"));
    }
  }

  @Test
  public void testSave_WithEmptyJson() throws ApplicationException {
    var ex = assertThrows(ValidationException.class, () -> this.service.save(new JSONObject()));
    assertThat(ex.getMessage(), is("Cannot save empty data"));
  }

  @Test
  public void testDelete() throws ApplicationException {
    this.service.delete("id");
    verify(this.repository, times(1)).delete("id");
  }

  @Test
  public void testDelete_WithEmptyId() throws ApplicationException {
    var ex = assertThrows(ValidationException.class, () -> this.service.delete(""));
    assertThat(ex.getMessage(), is("Id cannot be empty"));
  }

  @Test
  public void testFindId() throws ApplicationException {
    var result = new JSONObject("{\"domain\":\"domain\",\"id\":\"id\"}");
    doReturn(Optional.of(result)).when(this.repository).find("id");
    var json = this.service.find("id");
    assertThat(json.getString("id"), is("id"));
    assertThat(json.getString("domain"), is("domain"));
  }

  @Test
  public void testFindId_EmptyResult() throws ApplicationException {
    doReturn(Optional.empty()).when(this.repository).find("id");
    var json = this.service.find("id");
    assertTrue(json.isEmpty());
  }

  @Test
  public void testFindId_WithEmptyId() throws ApplicationException {
    var ex = assertThrows(ValidationException.class, () -> this.service.find(""));
    assertThat(ex.getMessage(), is("Id cannot be empty"));
  }

  @Test
  public void testFindCode() throws ApplicationException {
    var object = new JSONObject("{\"domain\":\"domain\",\"id\":\"id\",\"code\":\"code\"}");
    var result = new JSONArray().put(object);
    doReturn(Optional.of(result)).when(this.repository).find("code", "code");
    var json = this.service.findByCode("code");
    assertThat(json.getString("id"), is("id"));
    assertThat(json.getString("domain"), is("domain"));
    assertThat(json.getString("code"), is("code"));
  }

  @Test
  public void testFindCode_EmptyResult() throws ApplicationException {
    doReturn(Optional.empty()).when(this.repository).find("code", "code");
    var json = this.service.findByCode("code");
    assertTrue(json.isEmpty());
  }

  @Test
  public void testFindCode_WithEmptyCode() throws ApplicationException {
    var ex = assertThrows(ValidationException.class, () -> this.service.findByCode(""));
    assertThat(ex.getMessage(), is("Code cannot be empty"));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testFindList() throws ApplicationException {
    var object = new JSONObject("{\"domain\":\"domain\",\"id\":\"id\",\"code\":\"code\"}");
    var result = new JSONArray().put(object);
    doReturn(Optional.of(result)).when(this.repository).find(any(List.class));
    var jsons = this.service.find(List.of("id"));

    assertThat(jsons.length(), is(1));
    var json = (JSONObject) jsons.get(0);
    assertThat(json.getString("domain"), is("domain"));
    assertThat(json.getString("id"), is("id"));
    assertThat(json.getString("code"), is("code"));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testFindList_EmptyResult() throws ApplicationException {
    doReturn(Optional.empty()).when(this.repository).find(any(List.class));
    var json = this.service.find(List.of("id"));
    assertTrue(json.isEmpty());
  }

  @Test
  public void testFindList_WithEmptyList() throws ApplicationException {
    var ex = assertThrows(ValidationException.class, () -> this.service.find(List.of()));
    assertThat(ex.getMessage(), is("Keys cannot be empty"));
  }

  @Test
  public void testSearch() throws JSONException, ApplicationException {
    var criteria = "{\"criteria\": [{\"attribute\": \"name\",\"value\": \"Family\",\"operator\": \"contains\"}]}";
    doReturn(Optional.of(new JSONArray("[{\"name\":\"Given Family\"}]"))).when(this.index)
        .search(any(JSONObject.class));
    var result = this.service.search(new JSONObject(criteria));

    assertFalse(result.isEmpty());
  }

  @Test
  public void testSearch_EmptyResult() throws JSONException, ApplicationException {
    var criteria = "{\"criteria\": [{\"attribute\": \"name\",\"value\": \"Family\",\"operator\": \"contains\"}]}";
    doReturn(Optional.empty()).when(this.index).search(any(JSONObject.class));
    var result = this.service.search(new JSONObject(criteria));

    assertTrue(result.isEmpty());
  }
}
