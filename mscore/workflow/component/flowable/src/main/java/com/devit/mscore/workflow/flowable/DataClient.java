package com.devit.mscore.workflow.flowable;

import java.util.HashMap;
import java.util.Map;

import com.devit.mscore.ApplicationContext;
import com.devit.mscore.ServiceRegistration;
import com.devit.mscore.exception.ApplicationRuntimeException;
import com.devit.mscore.exception.RegistryException;
import com.devit.mscore.web.Client;

public class DataClient {

    private final Client client;

    private final ServiceRegistration serviceRegistration;

    private final String workflowDomain;

    private final String workflowTaskDomain;

    private Map<String, String> uriCache;

    public DataClient(Client client, ServiceRegistration serviceRegistration, String workflowDomain, String workflowTaskDomain) {
        this.client = client;
        this.serviceRegistration = serviceRegistration;
        this.workflowDomain = workflowDomain;
        this.workflowTaskDomain = workflowTaskDomain;
        this.uriCache = new HashMap<>();
    }

    public Client getClient() {
        return this.client.createNew();
    }

    public String getWorkflowUri(ApplicationContext context) {
        return getUri(context, this.workflowDomain);
    }

    public String getTaskUri(ApplicationContext context) {
        return getUri(context, this.workflowTaskDomain);
    }

    public String getDomainUri(ApplicationContext context, String domain) {
        return getUri(context, domain);
    }

    private String getUri(ApplicationContext context, String domain) {
        this.uriCache.computeIfAbsent(domain, key -> {
            try {
                return this.serviceRegistration.get(context, key);
            } catch (RegistryException ex) {
                throw new ApplicationRuntimeException(ex);
            }
        });

        return this.uriCache.get(domain);
    }
}
