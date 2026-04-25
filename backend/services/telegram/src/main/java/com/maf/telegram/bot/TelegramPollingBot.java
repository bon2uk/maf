package com.maf.telegram.bot;

import com.maf.telegram.config.TelegramProperties;
import com.maf.telegram.service.MessageService;
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
    private final MessageService messageService;
    private TelegramBotsLongPollingApplication application;
    private BotSession botSession;

    public TelegramPollingBot(TelegramProperties properties, MessageService messageService) {
        this.properties = properties;
        this.messageService = messageService;
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
        if (update.getMessage().hasText() && update.getMessage().getText().startsWith("/")) {
            log.info("Received command: {}", update.getMessage().getText());
            return;
        }

        Message message = update.getMessage();
        Chat chat = message.getChat();

        String chatTitle = chat.getTitle() != null ? chat.getTitle() : "Private Chat";
        Long senderId = message.getFrom() != null ? message.getFrom().getId() : 0L;
        String senderUsername = message.getFrom() != null && message.getFrom().getUserName() != null
                ? message.getFrom().getUserName() : "unknown";
        String text = message.getText() != null ? message.getText() : "";

        log.info(
                "Received message: chatId={}, chatType={}, chatTitle={}, userId={}, username={}, text={}",
                chat.getId(),
                chat.getType(),
                chatTitle,
                senderId,
                senderUsername,
                text
        );

        // Persist the raw message and emit the outbox event in one transaction.
        // Downstream parsing is fully async over Kafka — the bot loop is
        // intentionally lean so a slow parser can't back-pressure Telegram polling.
        try {
            messageService.createMessage(
                    message.getMessageId(),
                    chat.getId(),
                    chatTitle,
                    senderId,
                    senderUsername,
                    text
            );
        } catch (Exception e) {
            log.error("Failed to persist message: {}", e.getMessage(), e);
        }
    }
}
