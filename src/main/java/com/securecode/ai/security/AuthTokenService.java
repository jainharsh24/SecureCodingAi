package com.securecode.ai.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/** Issues compact, signed credentials for browser tabs. The token contains no platform data. */
@Service
public class AuthTokenService {
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private final byte[] secret;
    private final long lifetimeSeconds;

    public AuthTokenService(@Value("${auth.token-secret:change-this-development-secret-before-production}") String secret,
                            @Value("${auth.token-lifetime-seconds:28800}") long lifetimeSeconds) {
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        this.lifetimeSeconds = lifetimeSeconds;
    }

    public String issue(String email) {
        String payload = email + ":" + (Instant.now().getEpochSecond() + lifetimeSeconds);
        String encoded = Base64.getUrlEncoder().withoutPadding().encodeToString(payload.getBytes(StandardCharsets.UTF_8));
        return encoded + "." + signature(encoded);
    }

    public String authenticatedEmail(String token) {
        if (token == null || token.isBlank()) return null;
        String[] parts = token.split("\\.", -1);
        if (parts.length != 2 || !MessageDigest.isEqual(signature(parts[0]).getBytes(StandardCharsets.US_ASCII), parts[1].getBytes(StandardCharsets.US_ASCII))) return null;
        try {
            String[] payload = new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8).split(":", -1);
            if (payload.length != 2 || Instant.now().getEpochSecond() > Long.parseLong(payload[1])) return null;
            return payload[0];
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private String signature(String value) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secret, HMAC_ALGORITHM));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(value.getBytes(StandardCharsets.US_ASCII)));
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to sign authentication token", exception);
        }
    }
}
