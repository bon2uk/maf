package com.maf.telegram.entity;

import com.maf.telegram.model.MessageProcessingStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "telegram_messages")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Message {

    @Id
    @Setter(AccessLevel.NONE)
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, updatable = false)
    private Integer telegramMessageId;

    @Column(nullable = false, updatable = false)
    private Long chatId;

    @Column(nullable = false, length = 255)
    private String chatTitle;

    @Column(nullable = false, updatable = false)
    private Long senderId;

    @Column(name = "sender_username", nullable = false)
    private String senderUsername;

    @Column(nullable = false)
    private String text;

    @Column(name = "received_at", nullable = false, updatable = false)
    @Setter(AccessLevel.NONE)
    private Instant receivedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private MessageProcessingStatus status;

    @Column(name = "parse_error")
    private String parseError;

    @Column(name = "processed_at")
    private Instant processedAt;

    public static Message create(Integer telegramMessageId, Long chatId, String chatTitle,
                                   Long senderId, String senderUsername, String text) {
        return Message.builder()
                .telegramMessageId(telegramMessageId)
                .chatId(chatId)
                .chatTitle(chatTitle)
                .senderId(senderId)
                .senderUsername(senderUsername)
                .text(text)
                .receivedAt(Instant.now())
                .status(MessageProcessingStatus.NEW)
                .build();
    }

    public void markParsed() {
        if (this.status != MessageProcessingStatus.NEW) {
            throw new IllegalStateException(
                    "Cannot mark message as PARSED from status: " + this.status
            );
        }

        this.status = MessageProcessingStatus.PARSED;
        this.parseError = null;
        this.processedAt = Instant.now();
    }

}