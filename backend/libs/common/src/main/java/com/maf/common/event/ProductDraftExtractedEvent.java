package com.maf.common.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ProductDraftExtractedEvent(
        UUID sourceMessageId,
        Long telegramChatId,
        String telegramChatTitle,
        Long telegramSenderId,
        String telegramSenderUsername,
        String title,
        String description,
        BigDecimal price,
        String currency,
        String category,
        String parserModel,
        Instant occurredAt
) {
}
