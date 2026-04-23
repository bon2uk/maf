package com.maf.auth.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Provides a single {@link ObjectMapper} bean used across the service
 * (outbox payload serialization, Kafka JSON serializers, etc.).
 * <p>
 * Spring Boot 4's {@code spring-boot-starter-webmvc} does not bring in
 * Jackson autoconfiguration by default, so we wire a well-configured
 * mapper ourselves instead of relying on the starter to provide one.
 */
@Configuration
public class JacksonConfig {

    @Bean
    @ConditionalOnMissingBean
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
}
