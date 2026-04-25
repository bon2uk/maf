package com.maf.telegram.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maf.common.event.KafkaTopics;
import com.maf.common.event.ProductDraftExtractedEvent;
import com.maf.common.event.ProductParseFailedEvent;
import com.maf.telegram.entity.Message;
import com.maf.telegram.model.MessageProcessingStatus;
import com.maf.telegram.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;


@Slf4j
@Component
@RequiredArgsConstructor
public class ParserResultConsumer {

    private final MessageRepository messageRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = KafkaTopics.PRODUCT_DRAFT_EXTRACTED,
            groupId = "telegram-service",
            containerFactory = "kafkaListenerContainerFactory")
    @Transactional
    public void onDraftExtracted(String payload) throws Exception {
        ProductDraftExtractedEvent event = objectMapper.readValue(payload, ProductDraftExtractedEvent.class);
        log.info("Received ProductDraftExtracted for messageId={}", event.sourceMessageId());

        markMessage(event.sourceMessageId(),
                MessageProcessingStatus.PARSED,
                null,
                "draft extracted");
    }

    @KafkaListener(
            topics = KafkaTopics.PRODUCT_PARSE_FAILED,
            groupId = "telegram-service",
            containerFactory = "kafkaListenerContainerFactory")
    @Transactional
    public void onParseFailed(String payload) throws Exception {
        ProductParseFailedEvent event = objectMapper.readValue(payload, ProductParseFailedEvent.class);
        log.info("Received ProductParseFailed for messageId={}, kind={}",
                event.sourceMessageId(), event.kind());

        // INCOMPLETE_RESULT means the LLM responded fine but couldn't
        // extract a product — that's a permanent skip, not a retryable
        // failure, so we mark SKIPPED.
        MessageProcessingStatus terminalStatus = event.kind() == ProductParseFailedEvent.FailureKind.INCOMPLETE_RESULT
                ? MessageProcessingStatus.SKIPPED
                : MessageProcessingStatus.FAILED;

        markMessage(event.sourceMessageId(), terminalStatus, event.reason(), "parse failure");
    }

    private void markMessage(UUID messageId, MessageProcessingStatus status,
                             String reason, String context) {
        Optional<Message> maybe = messageRepository.findById(messageId);
        if (maybe.isEmpty()) {
            log.warn("Cannot apply {}: message {} not found", context, messageId);
            return;
        }

        Message message = maybe.get();
        if (message.getStatus() != MessageProcessingStatus.NEW) {
            log.info("Message {} already in terminal status {}, skipping {}",
                    messageId, message.getStatus(), context);
            return;
        }

        switch (status) {
            case PARSED -> message.markParsed();
            case SKIPPED -> message.markSkipped(reason);
            case FAILED -> message.markFailed(reason);
            default -> {
                log.warn("Unsupported terminal status {} for messageId={}", status, messageId);
                return;
            }
        }
        messageRepository.save(message);
    }
}
