package com.maf.parser.service;

import com.maf.parser.client.LlmClient;
import com.maf.parser.dto.LlmParseResponse;
import com.maf.parser.dto.ParsedProductResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ParserService {

    private final LlmClient llmClient;

    public ParsedProductResponse parseProductMessage(String message) {
        LlmParseResponse raw = llmClient.parse(message);
        LlmParseResponse.Parsed parsed = raw.parsed();

        if (parsed == null) {
            log.warn("llm-service returned a response without a 'parsed' object");
            return new ParsedProductResponse(null, null, null, null, null, raw.model());
        }

        return new ParsedProductResponse(
                parsed.title(),
                parsed.description(),
                parsed.price(),
                parsed.currency(),
                parsed.category(),
                raw.model()
        );
    }
}
