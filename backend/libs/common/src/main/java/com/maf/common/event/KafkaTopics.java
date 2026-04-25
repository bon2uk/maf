package com.maf.common.event;

public final class KafkaTopics {

    private KafkaTopics() {}

    /** Raw inbound Telegram message, ready for parsing. */
    public static final String TELEGRAM_MESSAGE_RECEIVED = "telegram.message-received";

    /** Successfully parsed product draft, ready for product-service to persist. */
    public static final String PRODUCT_DRAFT_EXTRACTED = "parser.product-draft-extracted";

    /** Parse attempt that produced no usable product (LLM failure, low confidence, missing fields). */
    public static final String PRODUCT_PARSE_FAILED = "parser.product-parse-failed";
}
