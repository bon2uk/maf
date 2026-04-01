\connect productdb

-- Products
CREATE TABLE telegram_messages (
    id UUID PRIMARY KEY,
    telegram_message_id INTEGER NOT NULL,
    chat_id BIGINT NOT NULL,
    chat_title VARCHAR(255) NOT NULL,
    sender_id BIGINT NOT NULL,
    sender_username VARCHAR(255) NOT NULL,
    text TEXT NOT NULL,
    received_at TIMESTAMP NOT NULL
);