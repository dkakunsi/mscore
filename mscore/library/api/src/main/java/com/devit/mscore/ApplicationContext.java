package com.devit.mscore;

import static com.devit.mscore.util.Utils.ACTION;
import static com.devit.mscore.util.Utils.AUTHORIZATION;
import static com.devit.mscore.util.Utils.BREADCRUMB_ID;
import static com.devit.mscore.util.Utils.PRINCIPAL;
import static com.devit.mscore.util.Utils.REQUESTED_BY;
import static com.devit.mscore.util.Utils.ROLE;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that encapsulate system information.
 * 
 * @author dkakunsi
 */
public abstract class ApplicationContext {

    private static final String UNKNOWN = "UNKNOWN";

    private static final String EMPTY = "";

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected Map<String, Object> contextData;

    protected ApplicationContext(Map<String, Object> contextData) {
        if (contextData != null) {
            this.contextData = contextData;
        } else {
            this.contextData = new HashMap<>();
        }
    }

    /**
     * Return the source of current request.
     * 
     * @return source of current request
     */
    public abstract String getSource();

    /**
     * Get breadcrumbId of the request context.
     * 
     * @return current breadcrumbId.
     */
    public String getBreadcrumbId() {
        return has(BREADCRUMB_ID) ? get(BREADCRUMB_ID).toString() : EMPTY;
    }

    /**
     * Get authenticated user that started the request.
     * 
     * @return authenticated user.
     */
    public String getRequestedBy() {
        return hasRequestedBy() ? getPrincipal().get().getString(REQUESTED_BY) : UNKNOWN;
    }

    protected boolean hasRequestedBy() {
        var principal = getPrincipal();
        return principal.isPresent() && principal.get().has(REQUESTED_BY);
    }

    /**
     * Get roles of authenticated user.
     * 
     * @return roles.
     */
    public List<Object> getUserRoles() {
        return hasUserRoles() ? getPrincipal().get().getJSONArray(ROLE).toList() : List.of();
    }

    protected boolean hasUserRoles() {
        var principal = getPrincipal();
        return principal.isPresent() && principal.get().has(ROLE);
    }

    /**
     * Get authenticated user's principal information.
     * 
     * @return authenticated user's info.
     */
    public Optional<JSONObject> getPrincipal() {
        return Optional.ofNullable(getJSONObject(PRINCIPAL));
    }

    /**
     * Get action. This will be used for notification template name. The value
     * resemblance the workflow id.
     * 
     * @return action name.
     */
    public Optional<String> getAction() {
        return Optional.ofNullable(getString(ACTION));
    }

    /**
     * Check whether action is available.
     * 
     * @return true if available, false otherwise.
     */
    protected boolean hasAction() {
        return has(ACTION);
    }

    /**
     * Retrieve the authentication token.
     * 
     * @return authentication token.
     */
    public Optional<String> getToken() {
        return Optional.ofNullable(getString(AUTHORIZATION));
    }

    /**
     * Check whether the request is authorized with specific {@code requiredRole}.
     * 
     * @param requiredRole the required role. Blank means authorize all.
     * @return true if user has the required authorization.
     */
    public boolean hasRole(String requiredRole) {
        return StringUtils.isBlank(requiredRole) || hasRole(List.of(requiredRole));
    }

    /**
     * Check whether the request is authorized with specific {@code requiredRoles}.
     * 
     * @param requiredRoles list of roles. Empty means authorize all.
     * @return true if one of the roles is acceptable, false otherwise.
     */
    public boolean hasRole(List<String> requiredRoles) {
        if (rolesIsEmpty(requiredRoles)) {
            return true;
        }

        var anyRequiredRolesExists = false;
        if (hasUserRoles()) {
            var currentUserRoles = getUserRoles();
            Predicate<Object> matchRequiredRoles = currentUserRole -> requiredRoles.contains(currentUserRole);
            anyRequiredRolesExists = currentUserRoles.stream().anyMatch(matchRequiredRoles);
        }
        return anyRequiredRolesExists;
    }

    private static final boolean rolesIsEmpty(List<String> roles) {
        return roles == null || roles.isEmpty();
    }

    private String getString(String key) {
        return has(key) ? get(key).toString() : null;
    }

    private JSONObject getJSONObject(String key) {
        return has(key) ? (JSONObject) get(key) : null;
    }

    private Object get(String key) {
        return this.contextData.get(key);
    }

    private boolean has(String key) {
        return this.contextData.containsKey(key);
    }

    /**
     * Convert the context to JSON.
     * 
     * @return json representation of the context.
     */
    public JSONObject toJson() {
        return new JSONObject(this.contextData);
    }

    @Override
    public String toString() {
        return toJson().toString();
    }

    protected void setPrincipal(String principal) {
        if (StringUtils.isBlank(principal)) {
            return;
        }

        var json = new JSONObject(principal);
        this.contextData.put(PRINCIPAL, json);
    }

    protected void setBreadcrumbId(String breadcrumbId) {
        this.contextData.put(BREADCRUMB_ID, breadcrumbId);
    }

    protected void generateBreadcrumbId() {
        this.contextData.put(BREADCRUMB_ID, UUID.randomUUID().toString());
    }
}
