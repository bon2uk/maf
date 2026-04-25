package com.maf.parser.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

/**
 * Connection and auth settings for talking to the Python llm-service.
 *
 * @param baseUrl       base URL of the llm-service (e.g. {@code http://llm-service:8091})
 * @param internalToken shared secret sent as {@code X-Internal-Token}; must
 *                      match {@code LLM_INTERNAL_TOKEN} on the llm-service
 * @param connectTimeout TCP connect timeout
 * @param readTimeout    read timeout (LLM inference is the bulk of this)
 * @param maxAttempts    total number of attempts (including the first) on
 *                      transient failures
 */
@Validated
@ConfigurationProperties(prefix = "parser.llm")
public record LlmProperties(
        @NotBlank String baseUrl,
        @NotBlank String internalToken,
        Duration connectTimeout,
        Duration readTimeout,
        @Positive int maxAttempts
) {
    public LlmProperties {
        if (connectTimeout == null) connectTimeout = Duration.ofSeconds(3);
        if (readTimeout == null) readTimeout = Duration.ofSeconds(60);
        if (maxAttempts == 0) maxAttempts = 2;
    }
}
