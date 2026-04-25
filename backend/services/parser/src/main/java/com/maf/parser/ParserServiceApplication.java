package com.maf.parser;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.ComponentScan;

/**
 * parser-service is a stateless HTTP facade over the Python llm-service;
 * it doesn't talk to a database. The JPA / DataSource / Kafka autoconfigs
 * that libs/common transitively drags in are disabled via
 * {@code spring.autoconfigure.exclude} in {@code application.yaml} — this
 * avoids leaking those starter dependencies into the parser's own pom.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
@ComponentScan(basePackages = {"com.maf.parser", "com.maf.common"})
public class ParserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ParserServiceApplication.class, args);
    }
}
