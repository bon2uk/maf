package com.maf.common.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ProductParseFailedEvent(
        UUID sourceMessageId,
        FailureKind kind,
        String reason,
        String parserModel,
        Instant occurredAt
) {

    public enum FailureKind {
        /** Upstream llm-service was unreachable or returned a non-2xx after retries. */
        LLM_UNAVAILABLE,
        /** LLM responded but the structured payload didn't contain a usable product. */
        INCOMPLETE_RESULT
    }
}
