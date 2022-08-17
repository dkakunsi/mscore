package com.devit.mscore.workflow.flowable.delegate;

import org.flowable.common.engine.api.delegate.Expression;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;

import com.devit.mscore.Logger;
import com.devit.mscore.logging.ApplicationLogger;

/**
 * Delegate that print message to log channle.
 * 
 * @author dkakunsi
 */
public class LogDelegate implements JavaDelegate {

  private static final Logger LOG = ApplicationLogger.getLogger(LogDelegate.class);

  private Expression message;

  private Expression level;

  @Override
  public void execute(DelegateExecution execution) {
    var messageValue = this.message.getValue(execution).toString();
    var levelValue = this.level == null ? "" : this.level.getValue(execution).toString();

    switch (levelValue.toLowerCase()) {
      case "info":
        LOG.debug(messageValue);
        break;
      case "warn":
        LOG.warn(messageValue);
        break;
      case "error":
        LOG.error(messageValue);
        break;
      default:
        LOG.info(messageValue);
    }
  }
}
