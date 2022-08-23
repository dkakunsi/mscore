package com.devit.mscore.data.synchronization;

import com.devit.mscore.Synchronization;
import com.devit.mscore.Synchronizer;
import com.devit.mscore.exception.SynchronizationException;

/**
 * <p>
 * Synchronization for {@code attribute} of {@code domain}.
 * </p>
 *
 * <ul>
 * <li><code>searchAttribute</code> is '<code>domain</code>.id'</li>
 * </ul>
 *
 * @author dkakunsi
 */
public class DefaultSynchronization extends Synchronization {

  public DefaultSynchronization(Synchronizer synchronizer, String referenceDomain, String referenceAttribute) {
    super(synchronizer, referenceDomain, referenceAttribute);
  }

  @Override
  public void synchronize(String referenceId) throws SynchronizationException {
    this.synchronizer.synchronize(getSearchAttribute(), referenceId);
  }
}
