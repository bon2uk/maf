CREATE TABLE IF NOT EXISTS outbox_events (
    id              UUID PRIMARY KEY,
    aggregate_type  VARCHAR(255) NOT NULL,
    aggregate_id    VARCHAR(255) NOT NULL,
    event_type      VARCHAR(255) NOT NULL,
    topic           VARCHAR(255) NOT NULL,
    payload         TEXT         NOT NULL,
    status          VARCHAR(16)  NOT NULL,
    attempts        INT          NOT NULL DEFAULT 0,
    last_error      TEXT,
    created_at      TIMESTAMPTZ  NOT NULL,
    processed_at    TIMESTAMPTZ,
    CONSTRAINT outbox_events_status_chk CHECK (status IN ('PENDING', 'PUBLISHED', 'FAILED'))
);

-- Partial index: the publisher only ever queries for PENDING rows, so this
-- stays tiny even after millions of PUBLISHED rows accumulate. It keeps the
-- scheduled poll O(log n) on just the PENDING subset.
CREATE INDEX IF NOT EXISTS idx_outbox_events_pending
    ON outbox_events (created_at)
    WHERE status = 'PENDING';
