package com.devit.mscore;

import com.devit.mscore.exception.SynchronizationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.json.JSONObject;

/**
 * <p>
 * Root class for synchronization process. Sync object that meet search
 * attribute using the given <code>synchronizer</code>
 * </p>
 *
 * @author dkakunsi
 */
public abstract class Synchronization implements Synchronizer {

  protected final Logger logger;

  /**
   * Domain that triggers this synchronization.
   * If object of this domain change, then we need to reindex all dependent domain.
   * 
   */
  protected String referenceDomain;

  protected String referenceAttribute;

  protected Optional<FiltersExecutor> filter;

  protected List<PostProcessObserver> observers;

  protected Synchronization(Logger logger, String referenceDomain, String referenceAttribute) {
    this.logger = logger;
    this.referenceDomain = referenceDomain;
    this.referenceAttribute = referenceAttribute;
    observers = new ArrayList<>();
    filter = Optional.empty();
  }

  public Synchronization with(FiltersExecutor filtersExecutor) {
    filter = Optional.of(filtersExecutor);
    return this;
  }

  public Synchronization with(PostProcessObserver observer) {
    observers.add(observer);
    return this;
  }

  public String getReferenceDomain() {
    return this.referenceDomain;
  }

  public String getReferenceAttribute() {
    return this.referenceAttribute;
  }

  protected String getSearchAttribute() {
    return String.format("%s.id", this.referenceAttribute);
  }

  @Override
  public void synchronize(String searchAttribute, String value) throws SynchronizationException {
    var jsons = loadFromDatastore(searchAttribute, value);
    if (jsons.isEmpty()) {
      logger.info("No data to synchronize in domain '{}'", getDomain());
      return;
    }
    for (Object object : jsons) {
      synchronize((JSONObject) object);
    }
  }

  private void synchronize(JSONObject json) {
    filter.ifPresent(f -> f.execute(json));
    this.observers.forEach(observer -> observer.notify(json));
  }

  @Override
  public void synchronize() throws SynchronizationException {
    // Just sync the parent, then system will propagate to all children.
    // Parent object is object without parent attribute,
    // which means it is children of no-one.
    synchronize("parent", null);
  }

  @Override
  public void synchronize(String id) throws SynchronizationException {
    synchronize(getSearchAttribute(), id);
  }

  protected abstract List<JSONObject> loadFromDatastore(String searchAttribute, String searchValue)
      throws SynchronizationException;

  /**
   * 
   * @return domain to be synchronized
   */
  protected abstract String getDomain();
}
