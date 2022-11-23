package com.devit.mscore.db.mongo;

import static com.devit.mscore.util.Constants.ID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.devit.mscore.Index;
import com.devit.mscore.Index.Criteria.Operator;
import com.devit.mscore.Repository;
import com.devit.mscore.exception.DataException;
import com.devit.mscore.exception.IndexingException;

import java.util.List;
import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

public class MongoIndexTest {

  private Repository repository;

  private MongoIndex index;

  @Before
  public void setup() {
    repository = mock(Repository.class);
    index = new MongoIndex("indexName", repository);
  }
  
  @Test
  public void givenValidDocument_WhenIndexingSuccessful_ThenShouldReturnId() throws DataException, IndexingException {
    var id = "id";
    var json = new JSONObject();
    json.put(ID, id);

    doReturn(json).when(repository).save(any(JSONObject.class));

    var createdId = index.index(json);
    assertThat(createdId, is(id));
  }

  @Test
  public void givenValidDocument_WhenIndexingFailed_ThenShouldThrowIndexingException() throws DataException {
    var id = "id";
    doThrow(DataException.class).when(repository).save(any(JSONObject.class));

    var json = new JSONObject();
    json.put(ID, id);

    assertThrows(IndexingException.class, () -> index.index(json));
  }

  @Test
  public void givenValidSearchCriteria_WhenSearchSuccessful_ThenShouldReturnResult() throws IndexingException, DataException {
    var array = new JSONArray();
    doReturn(Optional.of(array)).when(repository).findByCriteria(any());

    var criteria = new Index.Criteria(Operator.EQUALS, "code", "C001");
    var searchCriteria = new Index.SearchCriteria(List.of(criteria), 0, 0);
    var result = index.search(searchCriteria);
    assertThat(result.isPresent(), is(true));
  }

  @Test
  public void givenValidSearchCriteria_WhenSearchFailed_ThenShouldThrowIndexingException() throws DataException {
    doThrow(DataException.class).when(repository).findByCriteria(any());
    var criteria = new Index.Criteria(Operator.EQUALS, "code", "C001");
    var searchCriteria = new Index.SearchCriteria(List.of(criteria), 0, 0);
    assertThrows(IndexingException.class, () -> index.search(searchCriteria));
  }

  @Test
  public void givenValidSearchCriteriaAndOperatorIsContains_WhenSearchExecuted_ThenShouldThrowIndexingException() throws DataException {
    var criteria = new Index.Criteria(Operator.CONTAINS, "code", "C001");
    var searchCriteria = new Index.SearchCriteria(List.of(criteria), 0, 0);
    assertThrows(IndexingException.class, () -> index.search(searchCriteria));
    verify(repository, never()).find(anyString(), anyString());
  }

  @Test
  public void givenValidId_WhenRetrievingIsSuccessful_ThenShouldReturnResult() throws DataException, IndexingException {
    var id = "id";
    var json = new JSONObject();
    json.put(ID, id);

    doReturn(Optional.of(json)).when(repository).find(anyString());

    var result = index.get(id);
    assertThat(result.isPresent(), is(true));
  }

  @Test
  public void givenValidId_WhenRetrievingFailed_ThenShouldThrowIndexingException() throws DataException, IndexingException {
    doThrow(DataException.class).when(repository).find(anyString());
    assertThrows(IndexingException.class, () -> index.get("id"));
  }
}
