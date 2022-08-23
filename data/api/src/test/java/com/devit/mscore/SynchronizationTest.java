package com.devit.mscore;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

import com.devit.mscore.exception.SynchronizationException;

import org.junit.Test;

public class SynchronizationTest {

  @Test
  public void test() {
    var synchronizer = mock(Synchronizer.class);
    var synchronization = new SynchronizationImpl(synchronizer, "referenceDomain", "referenceAttribute");
    assertThat(synchronization.getReferenceDomain(), is("referenceDomain"));
    assertThat(synchronization.getReferenceAttribute(), is("referenceAttribute"));
    assertNotNull(synchronization.getSynchronizer());
    assertThat(synchronization.getSearchAttribute(), is("referenceAttribute.id"));
  }

  public class SynchronizationImpl extends Synchronization {

    protected SynchronizationImpl(Synchronizer synchronizer, String domain, String attribute) {
      super(synchronizer, domain, attribute);
    }

    @Override
    public void synchronize(String id) throws SynchronizationException {
    }
  }
}
