package com.devit.mscore;

import java.util.List;

import com.devit.mscore.exception.DataException;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

public class RepositoryTest {

  private Repository repository;

  @Before
  public void setup() {
    this.repository = new Repository() {
    };
  }

  @Test(expected = DataException.class)
  public void testSave() throws DataException {
    this.repository.save(new JSONObject());
  }

  @Test(expected = DataException.class)
  public void testDelete() throws DataException {
    this.repository.delete("");
  }

  @Test(expected = DataException.class)
  public void testFind() throws DataException {
    this.repository.find("");
  }

  @Test(expected = DataException.class)
  public void testFindList() throws DataException {
    this.repository.find(List.of());
  }

  @Test(expected = DataException.class)
  public void testFindByKey() throws DataException {
    this.repository.find("", "");
  }

  @Test(expected = DataException.class)
  public void testAll() throws DataException {
    this.repository.all();
  }
}
