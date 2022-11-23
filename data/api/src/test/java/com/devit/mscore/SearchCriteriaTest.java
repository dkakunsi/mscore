package com.devit.mscore;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThrows;

import com.devit.mscore.Index.SearchCriteria;
import com.devit.mscore.Index.SearchCriteria.Operator;
import com.devit.mscore.exception.IndexingException;

import org.json.JSONObject;
import org.junit.Test;

public class SearchCriteriaTest {

  private static final Operator OPERATOR = Operator.EQUALS;

  private static final String ATTRIBUTE = "code";

  private static final String VALUE = "V001";
  
  @Test
  public void givenBlankOperator_WhenValidatingSearchCriteria_ThenShouldThrowIndexingException() {
    var criteria = new Index.SearchCriteria(null, ATTRIBUTE, VALUE);
    assertThrows(IndexingException.class, () -> criteria.validate());
  }

  @Test
  public void givenBlankAttribute_WhenValidatingSearchCriteria_ThenShouldThrowIndexingException() {
    var criteria = new Index.SearchCriteria(OPERATOR, "", VALUE);
    assertThrows(IndexingException.class, () -> criteria.validate());
  }

  @Test
  public void givenBlankValue_WhenValidatingSearchCriteria_ThenShouldThrowIndexingException() {
    var criteria = new Index.SearchCriteria(OPERATOR, ATTRIBUTE, "");
    assertThrows(IndexingException.class, () -> criteria.validate());
  }

  @Test
  public void givenValidCriteriaJSON_WhenConversionRequested_ThenShouldRetrunSearchCriteria() {
    var jsonCriteria = new JSONObject();
    jsonCriteria.put(Index.SearchCriteria.ATTRIBUTE, ATTRIBUTE);
    jsonCriteria.put(Index.SearchCriteria.VALUE, VALUE);
    jsonCriteria.put(Index.SearchCriteria.OPERATOR, OPERATOR.toString());

    var searchCriteria = SearchCriteria.from(jsonCriteria);
    assertThat(searchCriteria.getAttribute(), is(ATTRIBUTE));
    assertThat(searchCriteria.getValue(), is(VALUE));
    assertThat(searchCriteria.getOperator(), is(Operator.EQUALS));
  }
}
