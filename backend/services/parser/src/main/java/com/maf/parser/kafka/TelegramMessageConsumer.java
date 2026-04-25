package com.maf.parser.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maf.common.event.KafkaTopics;
import com.maf.common.event.ProductDraftExtractedEvent;
import com.maf.common.event.ProductParseFailedEvent;
import com.maf.common.event.TelegramMessageReceivedEvent;
import com.maf.parser.dto.ParsedProductResponse;
import com.maf.parser.exception.LlmServiceException;
import com.maf.parser.outbox.OutboxService;
import com.maf.parser.service.ParserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class TelegramMessageConsumer {

    private static final String DRAFT_AGGREGATE_TYPE = "ProductDraft";
    private static final String DRAFT_EVENT_TYPE = "ProductDraftExtracted";
    private static final String FAILURE_EVENT_TYPE = "ProductParseFailed";

    private final ObjectMapper objectMapper;
    private final ParserService parserService;
    private final OutboxService outboxService;

    @KafkaListener(
            topics = KafkaTopics.TELEGRAM_MESSAGE_RECEIVED,
            groupId = "parser-service",
            containerFactory = "kafkaListenerContainerFactory")
    @Transactional
    public void onTelegramMessage(String payload) throws Exception {
        TelegramMessageReceivedEvent event = objectMapper.readValue(payload, TelegramMessageReceivedEvent.class);
        log.info("Parsing telegram message: messageId={}, telegramMessageId={}",
                event.messageId(), event.telegramMessageId());

        ParsedProductResponse parsed;
        try {
            parsed = parserService.parseProductMessage(event.text());
        } catch (LlmServiceException ex) {
            log.warn("llm-service unavailable for messageId={}: {}", event.messageId(), ex.getMessage());
            publishFailure(event, ProductParseFailedEvent.FailureKind.LLM_UNAVAILABLE,
                    ex.getMessage(), null);
            return;
        }

        // Reject obviously incomplete results before they reach product-service.
        // Both fields are needed to materialize a meaningful draft.
        if (parsed.title() == null || parsed.title().isBlank()) {
            publishFailure(event, ProductParseFailedEvent.FailureKind.INCOMPLETE_RESULT,
                    "llm-service returned no title", parsed.model());
            return;
        }
        if (parsed.price() == null) {
            publishFailure(event, ProductParseFailedEvent.FailureKind.INCOMPLETE_RESULT,
                    "llm-service returned no price", parsed.model());
            return;
        }

        publishDraft(event, parsed);
    }

    private void publishDraft(TelegramMessageReceivedEvent source, ParsedProductResponse parsed) {
        ProductDraftExtractedEvent draft = new ProductDraftExtractedEvent(
                source.messageId(),
                source.chatId(),
                source.chatTitle(),
                source.senderId(),
                source.senderUsername(),
                parsed.title(),
                parsed.description(),
                parsed.price(),
                parsed.currency() != null ? parsed.currency().name() : null,
                parsed.category() != null ? parsed.category().name() : null,
                parsed.model(),
                Instant.now()
        );
        outboxService.save(
                DRAFT_AGGREGATE_TYPE,
                source.messageId().toString(),
                DRAFT_EVENT_TYPE,
                KafkaTopics.PRODUCT_DRAFT_EXTRACTED,
                draft);
        log.info("Drafted product for messageId={}, title='{}', price={} {}",
                source.messageId(), parsed.title(), parsed.price(), parsed.currency());
    }

    private void publishFailure(TelegramMessageReceivedEvent source,
                                ProductParseFailedEvent.FailureKind kind,
                                String reason,
                                String model) {
        ProductParseFailedEvent failure = new ProductParseFailedEvent(
                source.messageId(),
                kind,
                reason,
                model,
                Instant.now()
        );
        outboxService.save(
                DRAFT_AGGREGATE_TYPE,
                source.messageId().toString(),
                FAILURE_EVENT_TYPE,
                KafkaTopics.PRODUCT_PARSE_FAILED,
                failure);
        log.info("Recorded parse failure for messageId={}, kind={}, reason={}",
                source.messageId(), kind, reason);
    }
}
