package com.maf.auth.kafka;

import com.maf.common.event.UserRegisteredEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j

@Component
@RequiredArgsConstructor
public class UserEventProducer {
    private static final String TOPIC = "auth.user-registered";
    private final KafkaTemplate<String, UserRegisteredEvent> kafkaTemplate;

    public void publishUserRegisteredEvent(UserRegisteredEvent event) {
        log.info("User registered event produced", event.id());

        kafkaTemplate.send(TOPIC, event.id().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish event for user {}", event.id(), ex);
                    } else {
                        log.info("Event published successfully for user {}, offset={}",
                                event.id(),
                                result.getRecordMetadata().offset());
                    }
                });
    }
}
