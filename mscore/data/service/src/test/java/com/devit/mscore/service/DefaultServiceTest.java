package com.devit.mscore.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Optional;

import com.devit.mscore.ApplicationContext;
import com.devit.mscore.Index;
import com.devit.mscore.Publisher;
import com.devit.mscore.Repository;
import com.devit.mscore.Schema;
import com.devit.mscore.enrichment.EnrichmentsExecutor;
import com.devit.mscore.filter.FiltersExecutor;
import com.devit.mscore.observer.IndexingObserver;
import com.devit.mscore.observer.PublishingObserver;
import com.devit.mscore.validation.ValidationsExecutor;
import com.devit.mscore.exception.ApplicationException;
import com.devit.mscore.exception.DataException;
import com.devit.mscore.exception.SynchronizationException;
import com.devit.mscore.exception.ValidationException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

public class DefaultServiceTest {

    private DefaultService service;

    private Repository repository;

    private Index index;

    private Publisher publisher;

    private Schema schema;

    private ApplicationContext context;

    @Before
    public void setup() {
        this.repository = mock(Repository.class);
        this.index = mock(Index.class);
        this.publisher = mock(Publisher.class);
        this.schema = mock(Schema.class);
        var validator = new ValidationsExecutor();
        var filter = new FiltersExecutor();
        var enricher = new EnrichmentsExecutor();
        var indexingObserver = new IndexingObserver(this.index);
        var publishingObserver = new PublishingObserver(this.publisher, 0L);
        this.service = new DefaultService(this.schema, this.repository, this.index, validator, filter, enricher)
                .addObserver(indexingObserver).addObserver(publishingObserver);

        doReturn("domain").when(schema).getDomain();
        assertThat(this.service.getDomain(), is("domain"));

        this.context = mock(ApplicationContext.class);
        doReturn("breadcrumbId").when(this.context).getBreadcrumbId();
    }

    @Test
    public void testSave() throws ApplicationException {
        var result = new JSONObject("{\"domain\":\"domain\",\"id\":\"newId\"}");
        doReturn(result).when(this.repository).save(any(ApplicationContext.class), any(JSONObject.class));
        var json = new JSONObject("{\"domain\":\"domain\"}");
        var id = this.service.save(this.context, json);

        assertThat(id, is("newId"));
        assertThat(json.getString("domain"), is("domain"));
    }

    @Test
    public void testSave_WithDependencies() throws ApplicationException {
        var result = new JSONObject("{\"domain\":\"domain\",\"id\":\"newId\"}");
        doReturn(result).when(this.repository).save(any(ApplicationContext.class), any(JSONObject.class));
        var json = new JSONObject("{\"domain\":\"domain\"}");
        var id = this.service.save(this.context, json);

        assertThat(id, is("newId"));
        assertThat(json.getString("domain"), is("domain"));
    }

    @Test
    public void testSave_WithEmptyJson() throws ApplicationException {
        var ex = assertThrows(ValidationException.class, () -> this.service.save(this.context, new JSONObject()));
        assertThat(ex.getMessage(), is("Cannot save empty data."));
    }

    @Test
    public void testDelete() throws ApplicationException {
        this.service.delete(this.context, "id");
        verify(this.repository, times(1)).delete(any(ApplicationContext.class), eq("id"));
    }

    @Test
    public void testDelete_WithEmptyId() throws ApplicationException {
        var ex = assertThrows(ValidationException.class, () -> this.service.delete(this.context, ""));
        assertThat(ex.getMessage(), is("Id cannot be empty."));
    }

    @Test
    public void testFindId() throws ApplicationException {
        var result = new JSONObject("{\"domain\":\"domain\",\"id\":\"id\"}");
        doReturn(Optional.of(result)).when(this.repository).find(any(ApplicationContext.class), eq("id"));
        var json = this.service.find(this.context, "id");
        assertThat(json.getString("id"), is("id"));
        assertThat(json.getString("domain"), is("domain"));
    }

    @Test
    public void testFindId_EmptyResult() throws ApplicationException {
        doReturn(Optional.empty()).when(this.repository).find(any(ApplicationContext.class), eq("id"));
        var json = this.service.find(this.context, "id");
        assertTrue(json.isEmpty());
    }

    @Test
    public void testFindId_WithEmptyId() throws ApplicationException {
        var ex = assertThrows(ValidationException.class, () -> this.service.find(this.context, ""));
        assertThat(ex.getMessage(), is("Id cannot be empty."));
    }

    @Test
    public void testFindCode() throws ApplicationException {
        var object = new JSONObject("{\"domain\":\"domain\",\"id\":\"id\",\"code\":\"code\"}");
        var result = new JSONArray().put(object);
        doReturn(Optional.of(result)).when(this.repository).find(any(ApplicationContext.class), eq("code"), eq("code"));
        var json = this.service.findByCode(this.context, "code");
        assertThat(json.getString("id"), is("id"));
        assertThat(json.getString("domain"), is("domain"));
        assertThat(json.getString("code"), is("code"));
    }

    @Test
    public void testFindCode_EmptyResult() throws ApplicationException {
        doReturn(Optional.empty()).when(this.repository).find(any(ApplicationContext.class), eq("code"), eq("code"));
        var json = this.service.findByCode(this.context, "code");
        assertTrue(json.isEmpty());
    }

    @Test
    public void testFindCode_WithEmptyCode() throws ApplicationException {
        var ex = assertThrows(ValidationException.class, () -> this.service.findByCode(this.context, ""));
        assertThat(ex.getMessage(), is("Code cannot be empty."));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testFindList() throws ApplicationException {
        var object = new JSONObject("{\"domain\":\"domain\",\"id\":\"id\",\"code\":\"code\"}");
        var result = new JSONArray().put(object);
        doReturn(Optional.of(result)).when(this.repository).find(any(ApplicationContext.class), any(List.class));
        var jsons = this.service.find(this.context, List.of("id"));

        assertThat(jsons.length(), is(1));
        var json = (JSONObject) jsons.get(0);
        assertThat(json.getString("domain"), is("domain"));
        assertThat(json.getString("id"), is("id"));
        assertThat(json.getString("code"), is("code"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testFindList_EmptyResult() throws ApplicationException {
        doReturn(Optional.empty()).when(this.repository).find(any(ApplicationContext.class), any(List.class));
        var json = this.service.find(this.context, List.of("id"));
        assertTrue(json.isEmpty());
    }

    @Test
    public void testFindList_WithEmptyList() throws ApplicationException {
        var ex = assertThrows(ValidationException.class, () -> this.service.find(this.context, List.of()));
        assertThat(ex.getMessage(), is("Keys cannot be empty."));
    }

    @Test
    public void testSynchronize() throws SynchronizationException, DataException {
        var object = new JSONObject("{\"domain\":\"domain\",\"id\":\"id\",\"code\":\"code\"}");
        var result = new JSONArray().put(object);
        doReturn(Optional.of(result)).when(this.repository).find(any(ApplicationContext.class), eq("parent"), any());
        doReturn(object).when(this.repository).save(any(ApplicationContext.class), any(JSONObject.class));
        this.service.synchronize(this.context);

        verify(this.repository).save(any(ApplicationContext.class), any(JSONObject.class));
    }

    @Test
    public void testSynchronizeId() throws SynchronizationException, DataException {
        var result = new JSONObject("{\"domain\":\"domain\",\"id\":\"id\",\"code\":\"code\"}");
        doReturn(Optional.of(result)).when(this.repository).find(any(ApplicationContext.class), eq("id"));
        doReturn(result).when(this.repository).save(any(ApplicationContext.class), any(JSONObject.class));
        this.service.synchronize(this.context, "id");

        verify(this.repository).save(any(ApplicationContext.class), any(JSONObject.class));
    }

    @Test
    public void testSynchronizeId_EmptyResult() throws SynchronizationException, DataException {
        doReturn(Optional.empty()).when(this.repository).find(any(ApplicationContext.class), eq("id"));
        this.service.synchronize(this.context, "id");

        verify(this.repository, never()).save(any(ApplicationContext.class), any(JSONObject.class));
    }

    @Test
    public void testSynchronizeId_ThrowsDataException() throws SynchronizationException, DataException {
        doThrow(new DataException("")).when(this.repository).find(any(ApplicationContext.class), eq("id"));
        var ex = assertThrows(SynchronizationException.class, () -> this.service.synchronize(this.context, "id"));
        assertThat(ex.getMessage(), is("Synchronization failed."));
        assertThat(ex.getCause(), instanceOf(DataException.class));
    }

    @Test
    public void testSynchronizeAttributeValue_ThrowsDataException() throws SynchronizationException, DataException {
        doThrow(new DataException("")).when(this.repository).find(any(ApplicationContext.class), eq("attribute"),
                eq("id"));
        var ex = assertThrows(SynchronizationException.class, () -> this.service.synchronize(this.context, "attribute", "id"));
        assertThat(ex.getMessage(), is("Synchronization failed."));
        assertThat(ex.getCause(), instanceOf(DataException.class));
    }

    @Test
    public void testSearch() throws JSONException, ApplicationException {
        var criteria = "{\"criteria\": [{\"attribute\": \"name\",\"value\": \"Family\",\"operator\": \"contains\"}]}";
        doReturn(Optional.of(new JSONArray("[{\"name\":\"Given Family\"}]"))).when(this.index)
                .search(any(ApplicationContext.class), any(JSONObject.class));
        var result = this.service.search(this.context, new JSONObject(criteria));

        assertFalse(result.isEmpty());
    }

    @Test
    public void testSearch_EmptyResult() throws JSONException, ApplicationException {
        var criteria = "{\"criteria\": [{\"attribute\": \"name\",\"value\": \"Family\",\"operator\": \"contains\"}]}";
        doReturn(Optional.empty()).when(this.index).search(any(ApplicationContext.class), any(JSONObject.class));
        var result = this.service.search(this.context, new JSONObject(criteria));

        assertTrue(result.isEmpty());
    }
}
