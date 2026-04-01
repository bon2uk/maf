package com.maf.telegram.parsing;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

@Component
public class ProductMessageParser {
    private static final Pattern PRICE_PATTERN = Pattern.compile(
            "(\\d+(?:[\\.,]\\d{1,2})?)\\s*(USD|UAH|EUR|грн|€|\\$)",
            Pattern.CASE_INSENSITIVE
    );

    public ParseResult parse(String message) {
        if(message == null || message.isBlank())  {
            return ParseResult.failure("Message text is empty");
        }

        String normalized = message.trim();
        String[] lines = normalized.split("\\R");
        String title = lines[0].trim();

        MatchResult match = PRICE_PATTERN.matcher(title);
        if(!match.hasMatch()) {
            return ParseResult.failure("Price not found in the title");
        }

        String rawPrice = match.group(1).replace(",", ".");
        String rawCurrency = match.group(2).toUpperCase();

        String currency = normalizeCurrency(rawCurrency);
        BigDecimal price = new BigDecimal(rawPrice);

        ParsedProductCandidate candidate = new ParsedProductCandidate(
                title,
                normalized,
                price,
                currency
        );

        return ParseResult.success(candidate);
    }


    private String normalizeCurrency(String rawCurrency) {
        return switch (rawCurrency) {
            case "$", "USD" -> "USD";
            case "€", "EUR" -> "EUR";
            case "ГРН", "UAH" -> "UAH";
            default -> rawCurrency;
        };
    }
}
