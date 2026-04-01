package com.maf.telegram.parsing;

public record ParseResult(
        boolean success,
        ParsedProductCandidate productCandidate,
        String errorMessage
) {
    public static ParseResult success(ParsedProductCandidate productCandidate) {
        return new ParseResult(true, productCandidate, null);
    }

    public static ParseResult failure(String errorMessage) {
        return new ParseResult(false, null, errorMessage);
    }
}
