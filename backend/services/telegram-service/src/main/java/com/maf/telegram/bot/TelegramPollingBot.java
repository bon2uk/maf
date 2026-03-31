package com.maf.telegram.bot;

import com.maf.telegram.config.TelegramProperties;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.BotSession;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.util.List;

@Slf4j
@Component
public class TelegramPollingBot implements LongPollingUpdateConsumer {

    private final TelegramProperties properties;
    private TelegramBotsLongPollingApplication application;
    private BotSession botSession;

    public TelegramPollingBot(TelegramProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    public void start() throws Exception {
        application = new TelegramBotsLongPollingApplication();
        botSession = application.registerBot(properties.botToken(), this);
        log.info("Telegram polling bot started: {}", properties.botUsername());
    }

    @PreDestroy
    public void stop() throws Exception {
        if (botSession != null) {
            botSession.close();
        }
        if (application != null) {
            application.close();
        }
    }

    @Override
    public void consume(List<Update> updates) {
        for (Update update : updates) {
            handleUpdate(update);
        }
    }

    private void handleUpdate(Update update) {
        if (!update.hasMessage()) {
            return;
        }

        Message message = update.getMessage();
        Chat chat = message.getChat();

        log.info(
                "Received message: chatId={}, chatType={}, chatTitle={}, userId={}, username={}, text={}",
                chat.getId(),
                chat.getType(),
                chat.getTitle(),
                message.getFrom() != null ? message.getFrom().getId() : null,
                message.getFrom() != null ? message.getFrom().getUserName() : null,
                message.getText()
        );
    }
}
