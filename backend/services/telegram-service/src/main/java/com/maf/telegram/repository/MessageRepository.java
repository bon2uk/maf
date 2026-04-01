package com.maf.telegram.repository;

import com.maf.telegram.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {
    Optional<Message> findByTelegramMessageId(Integer telegramMessageId);

    boolean existsByTelegramMessageId(Integer telegramMessageId);
}