package com.maf.telegram.repository;

import com.maf.telegram.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, UUID> {
    Optional<Message> findByTelegramMessageId(Integer telegramMessageId);

    boolean existsByTelegramMessageId(Integer telegramMessageId);
}