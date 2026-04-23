package com.maf.telegram;

import com.maf.telegram.config.TelegramProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(TelegramProperties.class)
@RequiredArgsConstructor
public class TelegramServiceApplication {


    public static void main(String[] args) {
        SpringApplication.run(TelegramServiceApplication.class, args);
    }

}
