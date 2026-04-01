package com.maf.telegram.service;

import com.maf.common.exception.EntityNotFoundException;
import com.maf.telegram.entity.Message;
import com.maf.telegram.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;


@Slf4j
@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;

    @Transactional
    public Message createMessage(Integer telegramMessageId, Long chatId, String chatTitle,
                                 Long senderId, String senderUsername, String text) {
        Message message = Message.create(telegramMessageId, chatId, chatTitle,
                senderId, senderUsername, text);
        Message saved = messageRepository.save(message);
        log.info("Saved message with id: {}", saved.getId());
        return saved;
    }

    public Message getMessageById(UUID id) {
        return messageRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Message", id));
    }
}