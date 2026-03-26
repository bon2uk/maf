package com.maf.user.kafka;

import com.maf.common.event.UserRegisteredEvent;
import com.maf.user.entity.User;
import com.maf.user.model.UserStatus;
import com.maf.user.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserRegisteredConsumer {

    private final UserRepository userRepository;

    @KafkaListener(topics = "auth.user-registered", groupId = "user-service")
    public void handle(UserRegisteredEvent event) {
        log.info("User registered event received", event.id());
        if (userRepository.existsById(event.id())) {
            log.info("User {} already exists, skipping", event.id());
            return;
        }

        User user = User.create(event.id(), event.email(), event.firstName(), event.lastName(), UserStatus.ACTIVE);
        userRepository.save(user);
    }

    @PostConstruct
    public void init() {
        log.info("UserRegisteredConsumer initialized");
    }
}
