package com.maf.telegram.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

/**
 * Connection settings for talking to the parser-service over its
 * {@code /internal/parser/**} HTTP API.
 *
 * <p>Authentication is handled separately via
 * {@link com.maf.telegram.security.ServiceJwtIssuer}: every request is
 * decorated with a freshly minted {@code SERVICE}-role JWT, so this record
 * doesn't carry any auth-related fields.
 *
 * @param baseUrl       base URL of the parser-service (e.g.
 *                      {@code http://parser-service:8092})
 * @param connectTimeout TCP connect timeout
 * @param readTimeout    read timeout (the parser fans out to the LLM, so
 *                      this should be generous)
 * @param maxAttempts    total number of attempts (including the first) on
 *                      transient failures
 */
@Validated
@ConfigurationProperties(prefix = "parser")
public record ParserProperties(
        @NotBlank String baseUrl,
        Duration connectTimeout,
        Duration readTimeout,
        @Positive int maxAttempts
) {
    public ParserProperties {
        if (connectTimeout == null) connectTimeout = Duration.ofSeconds(3);
        if (readTimeout == null) readTimeout = Duration.ofSeconds(60);
        if (maxAttempts == 0) maxAttempts = 2;
    }
}
