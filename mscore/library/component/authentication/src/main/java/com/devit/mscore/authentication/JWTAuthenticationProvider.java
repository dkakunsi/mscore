package com.devit.mscore.authentication;

import static com.devit.mscore.util.Utils.REQUESTED_BY;
import static com.devit.mscore.util.Utils.ROLE;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Map;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.devit.mscore.ApplicationContext;
import com.devit.mscore.AuthenticationProvider;
import com.devit.mscore.Configuration;
import com.devit.mscore.exception.ApplicationException;
import com.devit.mscore.exception.AuthenticationException;
import com.devit.mscore.exception.ConfigException;
import com.devit.mscore.exception.ImplementationException;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JWTAuthenticationProvider implements AuthenticationProvider {

    private static final Logger LOG = LoggerFactory.getLogger(JWTAuthenticationProvider.class);

    private static final String SECURE_URI = "services.%s.secure.uri";

    private static final String PUBLIC_KEY = "platform.keycloak.public.key";

    private Map<String, Object> uri;

    private RSAPublicKey publicKey;

    protected JWTAuthenticationProvider(ApplicationContext context, String publicKey, Map<String, Object> uri)
            throws ApplicationException {
        this.publicKey = publicKey(context, publicKey.getBytes());
        this.uri = uri;
    }

    private static RSAPublicKey publicKey(ApplicationContext context, byte[] byteKey) throws ApplicationException {
        try {
            var keySpec = new X509EncodedKeySpec(decode(byteKey));
            var keyFactory = KeyFactory.getInstance("RSA");
            return (RSAPublicKey) keyFactory.generatePublic(keySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
            LOG.error("BreadcrumbId: {}. Failed to create RSA public key.", context.getBreadcrumbId());
            throw new ApplicationException("Cannot create RSA key for authentication.", ex);
        }
    }

    @Override
    public JSONObject verify(ApplicationContext context, String key) throws AuthenticationException {
        if (StringUtils.isBlank(key)) {
            LOG.info("BreadcrumbId: {}. Session key is not provided.", context.getBreadcrumbId());
            return null;
        }

        LOG.debug("BreadcrumbId: {}. Verifying session: {}", context.getBreadcrumbId(), key);
        var token = key.replace("Bearer ", "");
        var algorithm = Algorithm.RSA256(this.publicKey, null);
        var verifier = JWT.require(algorithm).build();
        try {
            var jwt = verifier.verify(token);
            var payload = new JSONObject(decode(jwt.getPayload()));
            return getPrincipalData(context, payload);
        } catch (JWTVerificationException ex) {
            LOG.error("BreadcrumbId: {}. Token is not valid: {}", context.getBreadcrumbId(), token);
            throw new AuthenticationException("Token is not valid.", ex);
        }
    }

    private static String decode(String encoded) {
        var decoded = decode(encoded.getBytes());
        return new String(decoded);
    }

    private static byte[] decode(byte[] encoded) {
        return Base64.getDecoder().decode(encoded);
    }

    private static JSONObject getPrincipalData(ApplicationContext context, JSONObject token) {
        LOG.debug("BreadcrumbId: {}. Verified session: {}", context.getBreadcrumbId(), token);
        // @formatter:off
        return new JSONObject()
                .put(REQUESTED_BY, token.getString("name"))
                .put(ROLE, token.getJSONObject("realm_access").getJSONArray("roles"));
        // @formatter:on
    }

    @Override
    public void storeToken(ApplicationContext context, JSONObject token) throws ApplicationException {
        throw new ImplementationException("Not implemented yet.");
    }

    @Override
    public Map<String, Object> getUri() {
        return this.uri;
    }

    public static JWTAuthenticationProvider of(ApplicationContext context, Configuration configuration) throws ApplicationException {
        var secureUri = String.format(SECURE_URI, configuration.getServiceName());
        var publicKey = configuration.getConfig(context, PUBLIC_KEY).orElseThrow(() -> new ConfigException("Public key is not configured correctly."));
        try {
            if (!configuration.has(secureUri) || configuration.getConfig(context, secureUri).isEmpty()) {
                return new JWTAuthenticationProvider(context, publicKey, Map.of());
            }
            var uris = new JSONObject(configuration.getConfig(context, secureUri).orElse("")).toMap();
            return new JWTAuthenticationProvider(context, publicKey, uris);
        } catch (JSONException ex) {
            throw new ConfigException("Invalid security configuration.", ex);
        }
    }
}
