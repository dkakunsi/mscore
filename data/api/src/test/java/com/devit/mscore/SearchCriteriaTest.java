package com.devit.mscore;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThrows;

import com.devit.mscore.Index.Criteria.Operator;
import com.devit.mscore.Index.SearchCriteria;
import com.devit.mscore.exception.IndexingException;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

public class SearchCriteriaTest {
  
  @Test
  public void givenValidSearchCriteria_WhenConvertingFromJsonSuccessful_ThenShouldReturnObject() throws IndexingException {
    var jsonCriteria = new JSONObject();
    jsonCriteria.put("page", 0);
    // size by default is 10000
    var criterion = new JSONObject();
    criterion.put("operator", "equals");
    criterion.put("attribute", "id");
    criterion.put("value", "id01");
    var criteria = new JSONArray();
    criteria.put(criterion);
    jsonCriteria.put("criteria", criteria);
    
    var searchCriteria = SearchCriteria.from(jsonCriteria);

    assertThat(searchCriteria.getPage(), is(0));
    assertThat(searchCriteria.getSize(), is(10000));
    var listCriteria = searchCriteria.getCriteria();
    assertThat(listCriteria.size(), is(1));
    var criteriaObject = listCriteria.get(0);
    assertThat(criteriaObject.getAttribute(), is("id"));
    assertThat(criteriaObject.getValue(), is("id01"));
    assertThat(criteriaObject.getOperator(), is(Operator.EQUALS));

    searchCriteria.validate();
  }

  @Test
  public void givenNegativePage_WhenValidatingSearchCriteria_ThenShouldThrowIndexingException() {
    var jsonCriteria = new JSONObject();
    jsonCriteria.put("page", -1);
    // size by default is 10
    var criterion = new JSONObject();
    criterion.put("operator", "equals");
    criterion.put("attribute", "id");
    criterion.put("value", "id01");
    var criteria = new JSONArray();
    criteria.put(criterion);
    jsonCriteria.put("criteria", criteria);
    
    var searchCriteria = SearchCriteria.from(jsonCriteria);
    assertThrows(IndexingException.class, () -> searchCriteria.validate());
  }

  @Test
  public void givenNegativeSize_WhenValidatingSearchCriteria_ThenShouldThrowIndexingException() {
    var jsonCriteria = new JSONObject();
    jsonCriteria.put("page", 0);
    jsonCriteria.put("size", -1);
    var criterion = new JSONObject();
    criterion.put("operator", "equals");
    criterion.put("attribute", "id");
    criterion.put("value", "id01");
    var criteria = new JSONArray();
    criteria.put(criterion);
    jsonCriteria.put("criteria", criteria);
    
    var searchCriteria = SearchCriteria.from(jsonCriteria);
    assertThrows(IndexingException.class, () -> searchCriteria.validate());
  }
}
