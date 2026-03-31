package com.maf.telegram.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "telegram")
public record TelegramProperties(
        String botToken,
        String botUsername
) {
}
