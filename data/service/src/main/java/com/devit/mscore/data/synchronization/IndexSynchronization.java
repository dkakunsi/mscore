package com.devit.mscore.data.synchronization;

import com.devit.mscore.Index;
import com.devit.mscore.Index.Criteria;
import com.devit.mscore.Index.Criteria.Operator;
import com.devit.mscore.Index.SearchCriteria;
import com.devit.mscore.Resource;
import com.devit.mscore.Synchronization;
import com.devit.mscore.exception.IndexingException;
import com.devit.mscore.exception.SynchronizationException;
import com.devit.mscore.logging.ApplicationLogger;

import java.util.ArrayList;
import java.util.List;

import javax.naming.OperationNotSupportedException;

import org.json.JSONObject;

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
public class IndexSynchronization extends Synchronization {

  private Index index;

  public IndexSynchronization(Index index, String referenceDomain, String referenceAttribute) {
    super(ApplicationLogger.getLogger(IndexSynchronization.class), referenceDomain, referenceAttribute);
    this.index = index;
  }

  @Override
  public Resource getSchema() {
    throw new RuntimeException(new OperationNotSupportedException());
  }

  @Override
  public String getDomain() {
    return index.getName();
  }

  @Override
  protected List<JSONObject> loadFromDatastore(String searchAttribute, String searchValue)
      throws SynchronizationException {
    var query = createSearchCriteria(searchAttribute, searchValue);
    try {
      var arrayResult = index.search(query);
      if (arrayResult.isEmpty()) {
        return List.of();
      }
      var data = new ArrayList<JSONObject>();
      var arr = arrayResult.get();
      arr.forEach(a -> data.add((JSONObject) a));
      return data;
    } catch (IndexingException ex) {
      throw new SynchronizationException("Cannot sync document", ex);
    }
  }

  private SearchCriteria createSearchCriteria(String searchAttribute, String searchValue) {
    var criteria = new Criteria(Operator.EQUALS, searchAttribute, searchValue);
    return new SearchCriteria(List.of(criteria));
  }
}
