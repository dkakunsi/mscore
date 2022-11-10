package com.devit.mscore.workflow.flowable.delegate;

import org.flowable.common.engine.api.delegate.Expression;
import org.flowable.engine.delegate.DelegateExecution;

/**
 * Delegate that print message to log channel.
 *
 * @author dkakunsi
 */
public class LogMessage extends ApplicationDelegate {

  Expression level;

  @Override
  public void execute(DelegateExecution execution) {
    initContext(execution);

    var message = getValue(execution);
    var levelValue = getValue(execution, level);

    switch (levelValue.toLowerCase()) {
      case "info":
        logger.debug(message);
        break;
      case "warn":
        logger.warn(message);
        break;
      case "error":
        logger.error(message);
        break;
      default:
        logger.info(message);
    }
  }

  public void setLevel(Expression level) {
    this.level = level;
  }
}
