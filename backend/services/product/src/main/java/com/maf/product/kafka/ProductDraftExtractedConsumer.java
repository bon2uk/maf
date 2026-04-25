package com.maf.product.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maf.common.event.KafkaTopics;
import com.maf.common.event.ProductDraftExtractedEvent;
import com.maf.product.entity.Product;
import com.maf.product.model.CurrencyCode;
import com.maf.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductDraftExtractedConsumer {

    private final ObjectMapper objectMapper;
    private final ProductService productService;

    @KafkaListener(
            topics = KafkaTopics.PRODUCT_DRAFT_EXTRACTED,
            groupId = "product-service",
            containerFactory = "kafkaListenerContainerFactory")
    @Transactional
    public void onDraftExtracted(String payload) throws Exception {
        ProductDraftExtractedEvent event = objectMapper.readValue(payload, ProductDraftExtractedEvent.class);
        log.info("Received ProductDraftExtracted: messageId={}, title='{}'",
                event.sourceMessageId(), event.title());

        CurrencyCode currency = parseCurrency(event.currency());
        if (currency == null) {
            // Defensive: parser emits validated drafts, but a malformed
            // value should not poison the partition. Drop the record after
            // logging — an operator can replay from the failure topic if
            // they want to recover it.
            log.warn("Skipping draft messageId={}: unsupported currency '{}'",
                    event.sourceMessageId(), event.currency());
            return;
        }

        Product saved = productService.createDraft(
                event.sourceMessageId(),
                event.title(),
                event.description(),
                event.price(),
                currency,
                event.category(),
                event.parserModel()
        );
        log.info("Persisted product draft id={}, sourceMessageId={}",
                saved.getId(), event.sourceMessageId());
    }

    private static CurrencyCode parseCurrency(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return CurrencyCode.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}
