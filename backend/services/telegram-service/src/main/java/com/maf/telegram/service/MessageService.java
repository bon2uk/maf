package com.maf.telegram.service;

import com.maf.common.exception.EntityNotFoundException;
import com.maf.telegram.entity.Message;
import com.maf.telegram.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;

    public Message createMessage(Integer telegramMessageId, Long chatId, String chatTitle,
                                 Long senderId, String senderUsername, String text) {
        Message message = Message.create(telegramMessageId, chatId, chatTitle,
                senderId, senderUsername, text);
        return messageRepository.save(message);
    }

    public Message getMessageById(UUID id) {
        return messageRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Message", id));
    }
}