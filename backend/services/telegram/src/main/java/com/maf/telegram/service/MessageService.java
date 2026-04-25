package com.maf.telegram.service;

import com.maf.common.event.KafkaTopics;
import com.maf.common.event.TelegramMessageReceivedEvent;
import com.maf.common.exception.EntityNotFoundException;
import com.maf.telegram.entity.Message;
import com.maf.telegram.outbox.OutboxService;
import com.maf.telegram.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;


@Slf4j
@Service
@RequiredArgsConstructor
public class MessageService {

    private static final String MESSAGE_AGGREGATE_TYPE = "TelegramMessage";
    private static final String MESSAGE_RECEIVED_EVENT_TYPE = "TelegramMessageReceived";

    private final MessageRepository messageRepository;
    private final OutboxService outboxService;

    /**
     * Persists the raw Telegram message and emits a
     * {@link TelegramMessageReceivedEvent} to the outbox in the same
     * transaction. The scheduled {@code OutboxPublisher} ships it to Kafka,
     * where parser-service picks it up.
     */
    @Transactional
    public Message createMessage(Integer telegramMessageId, Long chatId, String chatTitle,
                                 Long senderId, String senderUsername, String text) {
        Message message = Message.create(telegramMessageId, chatId, chatTitle,
                senderId, senderUsername, text);
        Message saved = messageRepository.save(message);

        TelegramMessageReceivedEvent event = new TelegramMessageReceivedEvent(
                saved.getId(),
                saved.getTelegramMessageId(),
                saved.getChatId(),
                saved.getChatTitle(),
                saved.getSenderId(),
                saved.getSenderUsername(),
                saved.getText(),
                saved.getReceivedAt(),
                Instant.now()
        );
        outboxService.save(
                MESSAGE_AGGREGATE_TYPE,
                saved.getId().toString(),
                MESSAGE_RECEIVED_EVENT_TYPE,
                KafkaTopics.TELEGRAM_MESSAGE_RECEIVED,
                event);

        log.info("Saved message and outbox event, messageId={}", saved.getId());
        return saved;
    }

    public Message getMessageById(UUID id) {
        return messageRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Message", id));
    }
}
