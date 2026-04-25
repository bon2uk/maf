-- Idempotent baseline so fresh and existing telegramdb databases converge
-- on the same schema. Existing installs are seeded by the docker init
-- script in db/init/05_create_telegram_message_db.sql; new installs lean
-- on this migration. The IF NOT EXISTS guards keep both paths safe.
CREATE TABLE IF NOT EXISTS telegram_messages (
    id UUID PRIMARY KEY,
    telegram_message_id INTEGER NOT NULL,
    chat_id BIGINT NOT NULL,
    chat_title VARCHAR(255) NOT NULL,
    sender_id BIGINT NOT NULL,
    sender_username VARCHAR(255) NOT NULL,
    text TEXT NOT NULL,
    received_at TIMESTAMP NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'NEW',
    parse_error TEXT,
    processed_at TIMESTAMP
);
