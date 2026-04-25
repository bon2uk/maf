package com.maf.telegram.security;

import com.maf.telegram.config.ServiceJwtProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

/**
 * Mints (and caches) a short-lived service JWT that downstream services can
 * verify with the shared {@code JWT_SECRET_KEY}. The token carries a
 * {@code SERVICE} role so it clears the {@code hasRole("SERVICE")} matchers
 * guarding {@code /internal/**} routes.
 *
 * <p>The cached token is reused until it's within {@link #REFRESH_SKEW} of
 * its expiry, at which point the next call mints a new one. Token issuance
 * is cheap (HS512 over a few small claims) so we don't bother with
 * background refresh — the lazy approach keeps the wiring simple and
 * single-threaded enough for the bot's update loop.
 */
@Slf4j
@Component
public class ServiceJwtIssuer {

    /** Refresh slightly before actual expiry to absorb clock skew + RTT. */
    private static final long REFRESH_SKEW_SECONDS = 30;

    private final ServiceJwtProperties props;
    private final Key signingKey;

    private volatile String cachedToken;
    private volatile Instant cachedExpiresAt = Instant.EPOCH;

    public ServiceJwtIssuer(ServiceJwtProperties props) {
        this.props = props;
        this.signingKey = buildSigningKey(props.secretKey());
    }

    public synchronized String currentToken() {
        Instant now = Instant.now();
        if (cachedToken != null
                && now.isBefore(cachedExpiresAt.minusSeconds(REFRESH_SKEW_SECONDS))) {
            return cachedToken;
        }

        Instant expiresAt = now.plus(props.ttl());
        String token = Jwts.builder()
                .setSubject(props.subject())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiresAt))
                .claim("userId", props.userId().toString())
                .claim("roles", props.roles())
                .signWith(signingKey, SignatureAlgorithm.HS512)
                .compact();

        cachedToken = token;
        cachedExpiresAt = expiresAt;
        log.debug("Minted new service JWT, sub={}, roles={}, expiresAt={}",
                props.subject(), props.roles(), expiresAt);
        return token;
    }

    private static Key buildSigningKey(String base64Secret) {
        if (base64Secret == null || base64Secret.isBlank()) {
            throw new IllegalStateException(
                    "service-jwt.secret-key is not configured (set JWT_SECRET_KEY)");
        }
        byte[] keyBytes = Base64.getDecoder().decode(base64Secret);
        return new SecretKeySpec(keyBytes, SignatureAlgorithm.HS512.getJcaName());
    }
}
