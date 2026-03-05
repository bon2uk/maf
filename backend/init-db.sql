DO
$do$
BEGIN
   IF NOT EXISTS (
      SELECT FROM pg_catalog.pg_roles WHERE rolname = 'maf'
   ) THEN
      CREATE ROLE maf WITH LOGIN PASSWORD 'maf';
   END IF;
END
$do$;

-- Створюємо бази та призначаємо власником maf
CREATE DATABASE authdb OWNER maf;
CREATE DATABASE userdb OWNER maf;
CREATE DATABASE productdb OWNER maf;
CREATE DATABASE orderdb OWNER maf;
CREATE DATABASE cartdb OWNER maf;
CREATE DATABASE messagingdb OWNER maf;
CREATE DATABASE notificationdb OWNER maf;
CREATE DATABASE paymentdb OWNER maf;