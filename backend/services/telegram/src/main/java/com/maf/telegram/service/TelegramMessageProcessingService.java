package com.maf.telegram.service;

import com.maf.telegram.client.ParserClient;
import com.maf.telegram.client.ParserServiceException;
import com.maf.telegram.client.dto.ParsedProductResponse;
import com.maf.telegram.entity.Message;
import com.maf.telegram.parsing.ParseResult;
import com.maf.telegram.parsing.ParsedProductCandidate;
import com.maf.telegram.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramMessageProcessingService {

    private final MessageRepository messageRepository;
    private final ParserClient parserClient;

    @Transactional
    public void processMessage(Message message) {
        ParseResult result = invokeParser(message);

        if (!result.success()) {
            // The message was syntactically fine but the LLM couldn't extract
            // a product candidate (typical for chit-chat). Persist it as
            // SKIPPED so we don't keep retrying it on every restart.
            message.markSkipped(result.errorMessage());
            messageRepository.save(message);
            log.info("Message skipped, telegramMessageId={}, reason={}",
                    message.getTelegramMessageId(), result.errorMessage());
            return;
        }

        message.markParsed();
        messageRepository.save(message);

        log.info("Message parsed successfully, telegramMessageId={}, candidate={}",
                message.getTelegramMessageId(), result.productCandidate());
    }

    private ParseResult invokeParser(Message message) {
        try {
            ParsedProductResponse response = parserClient.parseProduct(message.getText());
            return toParseResult(response);
        } catch (ParserServiceException ex) {
            log.warn("parser-service call failed for telegramMessageId={}: {}",
                    message.getTelegramMessageId(), ex.getMessage());
            return ParseResult.failure("parser-service error: " + ex.getMessage());
        }
    }

    /**
     * Translates the parser-service response into the local {@link ParseResult}
     * domain. We treat a response without both a {@code title} and a {@code price}
     * as a "skip" — it's a usable signal that the LLM couldn't pin down a
     * product, and downstream consumers expect the candidate to be fully formed.
     */
    private ParseResult toParseResult(ParsedProductResponse response) {
        if (response.title() == null || response.title().isBlank()) {
            return ParseResult.failure("parser-service returned no title");
        }
        if (response.price() == null) {
            return ParseResult.failure("parser-service returned no price");
        }

        ParsedProductCandidate candidate = new ParsedProductCandidate(
                response.title(),
                response.description(),
                response.price(),
                response.currency()
        );
        return ParseResult.success(candidate);
    }
}
