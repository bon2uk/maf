package com.maf.common.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TelegramMessageReceivedEvent(
        UUID messageId,
        Integer telegramMessageId,
        Long chatId,
        String chatTitle,
        Long senderId,
        String senderUsername,
        String text,
        Instant receivedAt,
        Instant occurredAt
) {
}
