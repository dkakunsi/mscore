package com.devit.mscore.authentication;

import static com.devit.mscore.util.Constants.REQUESTED_BY;
import static com.devit.mscore.util.Constants.ROLE;

import com.devit.mscore.AuthenticationProvider;
import com.devit.mscore.Configuration;
import com.devit.mscore.Logger;
import com.devit.mscore.exception.ApplicationException;
import com.devit.mscore.exception.AuthenticationException;
import com.devit.mscore.exception.ConfigException;
import com.devit.mscore.exception.ImplementationException;
import com.devit.mscore.logging.ApplicationLogger;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;

public class JWTAuthenticationProvider implements AuthenticationProvider {

  private static final Logger LOG = ApplicationLogger.getLogger(JWTAuthenticationProvider.class);

  private static final String SECURE_URI = "services.%s.secure.uri";

  private static final String PUBLIC_KEY = "platform.keycloak.public.key";

  private Map<String, Object> uri;

  private RSAPublicKey publicKey;

  protected JWTAuthenticationProvider(String publicKey, Map<String, Object> uri)
      throws ConfigException, UnsupportedEncodingException {
    this.uri = uri;
    this.publicKey = publicKey(publicKey.getBytes(StandardCharsets.UTF_8.name()));
  }

  private static RSAPublicKey publicKey(byte[] byteKey) throws ConfigException {
    try {
      var keySpec = new X509EncodedKeySpec(decode(byteKey));
      var keyFactory = KeyFactory.getInstance("RSA");
      return (RSAPublicKey) keyFactory.generatePublic(keySpec);
    } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
      LOG.error("Failed to create RSA public key");
      throw new ConfigException("Cannot create RSA key for authentication", ex);
    }
  }

  @Override
  public JSONObject verify(String key) throws AuthenticationException {
    if (StringUtils.isBlank(key)) {
      LOG.info("Session key is not provided");
      return null;
    }

    LOG.debug("Verifying session with token '{}'", key);
    var token = key.replace("Bearer ", "");
    var algorithm = Algorithm.RSA256(publicKey, null);
    var verifier = JWT.require(algorithm).build();
    try {
      var jwt = verifier.verify(token);
      var payload = new JSONObject(decode(jwt.getPayload()));
      return getPrincipalData(payload);
    } catch (JWTVerificationException | JSONException | UnsupportedEncodingException ex) {
      LOG.error("Token is not valid: '{}'", token);
      throw new AuthenticationException("Token is not valid", ex);
    }
  }

  private static String decode(String encoded) throws UnsupportedEncodingException {
    var decoded = decode(encoded.getBytes(StandardCharsets.UTF_8.name()));
    return new String(decoded, StandardCharsets.UTF_8.name());
  }

  private static byte[] decode(byte[] encoded) {
    return Base64.getDecoder().decode(encoded);
  }

  private static JSONObject getPrincipalData(JSONObject token) {
    LOG.debug("Session is verified for token '{}'", token);
    return new JSONObject()
        .put(REQUESTED_BY, token.getString("name"))
        .put(ROLE, token.getJSONObject("realm_access").getJSONArray("roles"));
  }

  @Override
  public void storeToken(JSONObject token) throws ApplicationException {
    throw new ImplementationException("Not implemented yet");
  }

  @Override
  public Map<String, Object> getUri() {
    return new HashMap<>(uri);
  }

  public static JWTAuthenticationProvider of(Configuration configuration) throws ConfigException {
    var secureUri = String.format(SECURE_URI, configuration.getServiceName());
    var publicKey = configuration.getConfig(PUBLIC_KEY)
        .orElseThrow(() -> new ConfigException("Public key is not configured correctly"));
    try {
      if (!configuration.has(secureUri) || configuration.getConfig(secureUri).isEmpty()) {
        return new JWTAuthenticationProvider(publicKey, Map.of());
      }
      var uris = new JSONObject(configuration.getConfig(secureUri).orElse("")).toMap();
      return new JWTAuthenticationProvider(publicKey, uris);
    } catch (JSONException | UnsupportedEncodingException ex) {
      throw new ConfigException("Invalid security configuration", ex);
    }
  }
}
