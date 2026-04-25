\connect productdb

-- Products
-- user_id is nullable so DRAFT products produced by parser-service can
-- live without an owner until claimed.
-- source_message_id is set for parser-produced drafts and acts as the
-- idempotency key for re-deliveries from Kafka.
CREATE TABLE products (
    id                UUID PRIMARY KEY,
    name              VARCHAR(255) NOT NULL,
    description       TEXT,
    user_id           UUID,
    price             DECIMAL(19, 2) NOT NULL,
    currency          VARCHAR(3) NOT NULL,
    status            VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    image_url         VARCHAR(2048),
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    booked_by         UUID,
    booked_at         TIMESTAMPTZ,
    deleted_at        TIMESTAMPTZ,
    source_message_id UUID,
    category          VARCHAR(64),
    parser_model      VARCHAR(128),
    version           BIGINT NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX idx_products_source_message_id
    ON products (source_message_id)
    WHERE source_message_id IS NOT NULL;
