package com.devit.mscore.workflow.flowable;

import static com.devit.mscore.util.Utils.ACTION;
import static com.devit.mscore.util.Utils.BREADCRUMB_ID;

import java.util.HashMap;
import java.util.Map;

import com.devit.mscore.ApplicationContext;
import com.devit.mscore.Publisher;

import org.apache.commons.lang3.StringUtils;
import org.flowable.engine.delegate.DelegateExecution;

public class FlowableApplicationContext extends ApplicationContext {

  private FlowableApplicationContext(Map<String, Object> contextData) {
    super(contextData);
  }

  @Override
  public String getSource() {
    return "flowable";
  }

  public static ApplicationContext of(DelegateExecution execution) {
    var contextData = new HashMap<String, Object>();
    var context = new FlowableApplicationContext(contextData);
    context.breadcrumbId(execution);
    context.action(execution);

    return context;
  }

  private void action(DelegateExecution execution) {
    var processDefinitionId = execution.getProcessDefinitionId();
    var action = processDefinitionId.split(":")[0];
    if (!StringUtils.isBlank(action)) {
      this.contextData.put(ACTION, action);
    }
  }

  private void breadcrumbId(DelegateExecution execution) {
    var variableObj = execution.getVariable(BREADCRUMB_ID);
    if (variableObj != null) {
      setBreadcrumbId(variableObj.toString());
    }
  }

  public DataClient getDataClient() {
    return DelegateUtils.getDataClient();
  }

  public Publisher getPublisher(String target) {
    return DelegateUtils.getPublisher(target);
  }
}
