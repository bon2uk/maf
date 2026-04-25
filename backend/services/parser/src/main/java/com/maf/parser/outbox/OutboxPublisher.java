package com.maf.parser.outbox;

import com.maf.parser.entity.OutboxEvent;
import com.maf.parser.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxPublisher {

    private static final int BATCH_SIZE = 50;
    private static final long SEND_TIMEOUT_SECONDS = 10L;

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> stringKafkaTemplate;

    @Scheduled(fixedDelayString = "${parser.outbox.poll-delay-ms:1000}")
    @Transactional
    public void publishPending() {
        List<OutboxEvent> batch = outboxEventRepository.findBatchForPublishing(
                OutboxEvent.Status.PENDING, PageRequest.of(0, BATCH_SIZE));

        if (batch.isEmpty()) {
            return;
        }

        log.debug("Outbox: publishing {} pending events", batch.size());

        for (OutboxEvent event : batch) {
            try {
                CompletableFuture<SendResult<String, String>> future =
                        stringKafkaTemplate.send(event.getTopic(), event.getAggregateId(), event.getPayload());
                SendResult<String, String> result = future.get(SEND_TIMEOUT_SECONDS, TimeUnit.SECONDS);

                event.setStatus(OutboxEvent.Status.PUBLISHED);
                event.setProcessedAt(Instant.now());
                event.setLastError(null);
                event.setAttempts(event.getAttempts() + 1);

                log.info("Outbox: published event {} ({}), offset={}",
                        event.getId(),
                        event.getEventType(),
                        result.getRecordMetadata().offset());
            } catch (Exception ex) {
                event.setAttempts(event.getAttempts() + 1);
                event.setLastError(truncate(ex.getMessage()));
                log.warn("Outbox: failed to publish event {} (attempt {}): {}",
                        event.getId(), event.getAttempts(), ex.getMessage());
            }
        }
    }

    private static String truncate(String s) {
        if (s == null) return null;
        return s.length() > 1000 ? s.substring(0, 1000) : s;
    }
}
