package com.devit.mscore;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThrows;

import com.devit.mscore.Index.Criteria;
import com.devit.mscore.Index.Criteria.Operator;
import com.devit.mscore.exception.IndexingException;

import org.json.JSONObject;
import org.junit.Test;

public class CriteriaTest {

  private static final Operator OPERATOR = Operator.EQUALS;

  private static final String ATTRIBUTE = "code";

  private static final String VALUE = "V001";
  
  @Test
  public void givenBlankOperator_WhenValidatingSearchCriteria_ThenShouldThrowIndexingException() {
    var criteria = new Criteria(null, ATTRIBUTE, VALUE);
    assertThrows(IndexingException.class, () -> criteria.validate());
  }

  @Test
  public void givenBlankAttribute_WhenValidatingSearchCriteria_ThenShouldThrowIndexingException() {
    var criteria = new Criteria(OPERATOR, "", VALUE);
    assertThrows(IndexingException.class, () -> criteria.validate());
  }

  @Test
  public void givenBlankValue_WhenValidatingSearchCriteria_ThenShouldThrowIndexingException() {
    var criteria = new Criteria(OPERATOR, ATTRIBUTE, "");
    assertThrows(IndexingException.class, () -> criteria.validate());
  }

  @Test
  public void givenValidCriteriaJSON_WhenConversionRequested_ThenShouldRetrunSearchCriteria() {
    var jsonCriteria = new JSONObject();
    jsonCriteria.put(Criteria.ATTRIBUTE, ATTRIBUTE);
    jsonCriteria.put(Criteria.VALUE, VALUE);
    jsonCriteria.put(Criteria.OPERATOR, OPERATOR.toString());

    var searchCriteria = Criteria.from(jsonCriteria);
    assertThat(searchCriteria.getAttribute(), is(ATTRIBUTE));
    assertThat(searchCriteria.getValue(), is(VALUE));
    assertThat(searchCriteria.getOperator(), is(Operator.EQUALS));
  }
}
