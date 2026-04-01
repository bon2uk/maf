package com.maf.telegram.service;

import com.maf.telegram.entity.Message;
import com.maf.telegram.parsing.ParseResult;
import com.maf.telegram.parsing.ProductMessageParser;
import com.maf.telegram.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramMessageProcessingService {
    private final MessageRepository messageRepository;
    private final ProductMessageParser productMessageParser;

    public void processMessage(Message message) {
        ParseResult parseResult = productMessageParser.parse(message.getText());

        if(!parseResult.success()) {
            messageRepository.save(message);
            log.info("Message skipped, telegramMessageId={}, reason={}",
                    message.getTelegramMessageId(), parseResult.failure("failed to parse message"));
            return;
        }
        message.markParsed();
        messageRepository.save(message);

        log.info("Message parsed successfully, telegramMessageId={}",
                message.getTelegramMessageId());

    }
}
