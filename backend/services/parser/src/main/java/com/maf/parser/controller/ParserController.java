package com.maf.parser.controller;

import com.maf.parser.dto.ParseMessageRequest;
import com.maf.parser.dto.ParsedProductResponse;
import com.maf.parser.service.ParserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/internal/parser")
@RequiredArgsConstructor
@Validated
public class ParserController {

    private final ParserService parserService;

    /**
     * Parses a free-form user message into structured product attributes
     * via the upstream llm-service. Any authenticated caller may use this
     * endpoint — frontends can call it on behalf of the logged-in user,
     * and other services can call it with their SERVICE-role JWT.
     */
    @PostMapping("/product")
    public ResponseEntity<ParsedProductResponse> parseProduct(
            @Valid @RequestBody ParseMessageRequest request) {
        return ResponseEntity.ok(parserService.parseProductMessage(request.message()));
    }
}
