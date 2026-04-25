package com.maf.parser.client;

import com.maf.parser.config.LlmProperties;
import com.maf.parser.dto.LlmParseResponse;
import com.maf.parser.exception.LlmServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.Map;

/**
 * Thin HTTP client for the Python llm-service.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Call {@code POST /internal/llm/parse} with the shared-secret header.
 *   <li>Translate transient upstream errors ({@link ResourceAccessException},
 *       5xx) into {@link LlmServiceException} after bounded retries.
 *   <li>Leave non-retryable errors (4xx from the LLM service) as a single
 *       attempt; they indicate client-side bugs that retries won't fix.
 * </ul>
 */
@Slf4j
@Component
public class LlmClient {

    private static final String PARSE_PATH = "/internal/llm/parse";

    private final RestClient restClient;
    private final LlmProperties props;

    public LlmClient(@Qualifier("llmRestClient") RestClient restClient,
                     LlmProperties props) {
        this.restClient = restClient;
        this.props = props;
    }

    public LlmParseResponse parse(String message) {
        Map<String, String> body = Map.of("message", message);
        RuntimeException last = null;

        for (int attempt = 1; attempt <= props.maxAttempts(); attempt++) {
            try {
                LlmParseResponse response = restClient.post()
                        .uri(PARSE_PATH)
                        .body(body)
                        .retrieve()
                        .body(LlmParseResponse.class);

                if (response == null) {
                    throw new LlmServiceException(
                            "llm-service returned an empty body for " + PARSE_PATH);
                }
                if (attempt > 1) {
                    log.info("llm-service call succeeded on attempt {}", attempt);
                }
                return response;
            } catch (RestClientResponseException ex) {
                last = ex;
                if (ex.getStatusCode().is4xxClientError()) {
                    // 4xx is our fault — don't waste retries on it.
                    log.warn("llm-service responded with {} on attempt {}: {}",
                            ex.getStatusCode().value(), attempt, ex.getResponseBodyAsString());
                    throw new LlmServiceException(
                            "llm-service rejected the request: " + ex.getStatusCode(), ex);
                }
                log.warn("llm-service 5xx on attempt {}/{}: {}",
                        attempt, props.maxAttempts(), ex.getStatusCode());
            } catch (ResourceAccessException ex) {
                last = ex;
                log.warn("llm-service transport error on attempt {}/{}: {}",
                        attempt, props.maxAttempts(), ex.getMessage());
            }
        }

        throw new LlmServiceException(
                "llm-service unavailable after " + props.maxAttempts() + " attempts", last);
    }
}
