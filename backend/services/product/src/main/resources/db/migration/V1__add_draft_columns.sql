-- Adds the columns and constraints required for the parser-service
-- pipeline. user_id becomes nullable so DRAFT products can exist before
-- a user claims them; source_message_id ties drafts back to the
-- originating telegram_messages row and acts as the idempotency key for
-- the kafka consumer.

ALTER TABLE products
    ALTER COLUMN user_id DROP NOT NULL;

ALTER TABLE products
    ADD COLUMN IF NOT EXISTS source_message_id UUID,
    ADD COLUMN IF NOT EXISTS category VARCHAR(64),
    ADD COLUMN IF NOT EXISTS parser_model VARCHAR(128);

-- Unique partial index: enforces idempotency for drafts produced via the
-- ProductDraftExtracted topic, while leaving regular ACTIVE products
-- (no source_message_id) unconstrained.
CREATE UNIQUE INDEX IF NOT EXISTS idx_products_source_message_id
    ON products (source_message_id)
    WHERE source_message_id IS NOT NULL;
