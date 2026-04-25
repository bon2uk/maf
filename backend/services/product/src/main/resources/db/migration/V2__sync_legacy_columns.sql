-- Idempotently brings the products table in sync with the JPA entity for
-- legacy databases that pre-date Flyway in this service. Existing
-- installs were originally created by db/init/04_create_product_db.sql
-- with only a handful of columns, and additional fields were either
-- backfilled by hibernate ddl-auto=update or never created at all.
--
-- Now that the service runs with ddl-auto=validate, every column referenced
-- by the entity must exist in the database. All ADDs use IF NOT EXISTS so
-- the migration is a no-op for fresh installs (where V0 / the new init
-- script already created the full table).

ALTER TABLE products
    ADD COLUMN IF NOT EXISTS status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    ADD COLUMN IF NOT EXISTS image_url VARCHAR(2048),
    ADD COLUMN IF NOT EXISTS booked_by UUID,
    ADD COLUMN IF NOT EXISTS booked_at TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;
