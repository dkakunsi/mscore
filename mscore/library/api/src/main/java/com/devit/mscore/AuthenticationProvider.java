package com.devit.mscore;

import java.util.Map;

import com.devit.mscore.exception.ApplicationException;
import com.devit.mscore.exception.AuthenticationException;

import org.json.JSONObject;

/**
 * Object to maintain access to service. If the session is invalid then throws
 * {@link AuthenticationException}.
 * 
 * @author dkakunsi
 */
public interface AuthenticationProvider {

  /**
   * Verify that session with {@code key} is valid. Valid mean exists in session
   * store and not expire.
   * 
   * @param key session key.
   * @return principal
   */
  JSONObject verify(String key) throws AuthenticationException;

  /**
   * Store token in session store on success login.
   * 
   * @param token to add.
   * @throws DataException error when connecting to session store.
   */
  void storeToken(JSONObject token) throws ApplicationException;

  /**
   * Get the uri that requires authentication.
   * 
   * @return uri.
   */
  Map<String, Object> getUri();
}
