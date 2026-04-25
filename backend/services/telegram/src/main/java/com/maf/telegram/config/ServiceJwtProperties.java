package com.maf.telegram.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

/**
 * Settings for the short-lived JWT that telegram-service mints to call other
 * services' {@code /internal/**} endpoints. The token is signed with
 * {@code JWT_SECRET_KEY} (HS512) — the same secret all services share — so
 * downstream {@code com.maf.common.security.JwtTokenValidator} can verify it
 * without any extra configuration.
 *
 * @param secretKey base64-encoded HS512 key, identical to the one configured
 *                  on auth/user/parser services
 * @param subject   {@code sub} claim (typically the service name)
 * @param userId    {@code userId} claim — must be a UUID because the common
 *                  filter calls {@code UUID.fromString(userId)}; using a
 *                  deterministic UUID derived from the service name keeps it
 *                  stable across restarts
 * @param roles     roles claim; must include {@code SERVICE} to clear
 *                  parser-service's {@code hasRole("SERVICE")} matcher
 * @param ttl       lifetime of each minted token; the issuer refreshes ahead
 *                  of expiry
 */
@Validated
@ConfigurationProperties(prefix = "service-jwt")
public record ServiceJwtProperties(
        @NotBlank String secretKey,
        @NotBlank String subject,
        @NotNull UUID userId,
        @NotEmpty List<String> roles,
        Duration ttl
) {
    public ServiceJwtProperties {
        if (ttl == null) ttl = Duration.ofMinutes(15);
    }
}
