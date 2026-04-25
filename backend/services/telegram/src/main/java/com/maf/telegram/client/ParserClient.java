package com.maf.telegram.client;

import com.maf.telegram.client.dto.ParseProductRequest;
import com.maf.telegram.client.dto.ParsedProductResponse;
import com.maf.telegram.config.ParserProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

/**
 * Thin HTTP client for the parser-service.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Call {@code POST /internal/parser/product} with the configured
 *       Bearer token (when present).
 *   <li>Retry transient transport / 5xx errors up to
 *       {@link ParserProperties#maxAttempts()} times.
 *   <li>Surface 4xx as a single non-retryable {@link ParserServiceException}
 *       — those indicate caller-side bugs that retries won't fix.
 * </ul>
 */
@Slf4j
@Component
public class ParserClient {

    private static final String PARSE_PATH = "/internal/parser/product";

    private final RestClient restClient;
    private final ParserProperties props;

    public ParserClient(@Qualifier("parserRestClient") RestClient restClient,
                        ParserProperties props) {
        this.restClient = restClient;
        this.props = props;
    }

    public ParsedProductResponse parseProduct(String message) {
        ParseProductRequest body = new ParseProductRequest(message);
        RuntimeException last = null;

        for (int attempt = 1; attempt <= props.maxAttempts(); attempt++) {
            try {
                ParsedProductResponse response = restClient.post()
                        .uri(PARSE_PATH)
                        .body(body)
                        .retrieve()
                        .body(ParsedProductResponse.class);

                if (response == null) {
                    throw new ParserServiceException(
                            "parser-service returned an empty body for " + PARSE_PATH);
                }
                if (attempt > 1) {
                    log.info("parser-service call succeeded on attempt {}", attempt);
                }
                return response;
            } catch (RestClientResponseException ex) {
                last = ex;
                if (ex.getStatusCode().is4xxClientError()) {
                    log.warn("parser-service responded with {} on attempt {}: {}",
                            ex.getStatusCode().value(), attempt, ex.getResponseBodyAsString());
                    throw new ParserServiceException(
                            "parser-service rejected the request: " + ex.getStatusCode(), ex);
                }
                log.warn("parser-service 5xx on attempt {}/{}: {}",
                        attempt, props.maxAttempts(), ex.getStatusCode());
            } catch (ResourceAccessException ex) {
                last = ex;
                log.warn("parser-service transport error on attempt {}/{}: {}",
                        attempt, props.maxAttempts(), ex.getMessage());
            }
        }

        throw new ParserServiceException(
                "parser-service unavailable after " + props.maxAttempts() + " attempts", last);
    }
}
