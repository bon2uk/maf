package com.maf.telegram.dto;

import java.time.Instant;

public record CreateMessageRequest(
        Integer telegramMessageId,
        Long chatId,
        String chatTitle,
        Long senderId,
        String senderUsername,
        String text,
        Instant receivedAt
) {}
