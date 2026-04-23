package com.maf.telegram.parsing;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ProductMessageParser {
    private static final Pattern PRICE_PATTERN = Pattern.compile(
            "(\\d+(?:[.,]\\d{1,2})?)\\s*(USD|UAH|EUR|грн|€|\\$)",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
    );

    public ParseResult parse(String message) {
        if (message == null || message.isBlank()) {
            return ParseResult.failure("Message text is empty");
        }

        String normalized = message.trim();
        String[] lines = normalized.split("\\R");
        String title = lines[0].trim();

        // Search for price in the entire message, not just the first line
        Matcher matcher = PRICE_PATTERN.matcher(normalized);
        if (!matcher.find()) {
            return ParseResult.failure("Price not found in the message");
        }

        String rawPrice = matcher.group(1).replace(",", ".");
        String rawCurrency = matcher.group(2).toUpperCase();

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
        String upper = rawCurrency.toUpperCase();
        return switch (upper) {
            case "$", "USD" -> "USD";
            case "€", "EUR" -> "EUR";
            case "ГРН", "UAH" -> "UAH";
            default -> upper;
        };
    }
}
