-- Email verification for DaycareLog
--
-- Reference migration only: this project uses Hibernate's ddl-auto=update to
-- manage schema (see backend/src/main/resources/application.properties), not
-- Flyway/Liquibase. Hibernate will apply the equivalent DDL automatically on
-- next boot from the JPA entities (User.emailVerified, VerificationToken).
-- This file documents exactly what that DDL is, and can be run by hand against
-- Supabase if you want the schema to exist before the app's first boot.

-- DEFAULT true is deliberate: existing accounts (created before this feature
-- shipped) must not be locked out. New accounts that require verification set
-- this to false explicitly in application code (see AuthService.register,
-- GuardianService.addGuardian).
ALTER TABLE users
    ADD COLUMN IF NOT EXISTS email_verified BOOLEAN NOT NULL DEFAULT true;

CREATE TABLE IF NOT EXISTS verification_tokens (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash  VARCHAR(64) NOT NULL,      -- SHA-256 hex digest; raw token/code is never stored
    type        VARCHAR(20) NOT NULL,      -- 'EMAIL_LINK' | 'EMAIL_CODE'
    expires_at  TIMESTAMP NOT NULL,
    consumed_at TIMESTAMP,
    attempts    INTEGER NOT NULL DEFAULT 0, -- EMAIL_CODE only: wrong-guess counter
    created_at  TIMESTAMP NOT NULL DEFAULT now()
);

-- Looking up a token/code by its hash (verify-email) and the resend rate-limit
-- query (count recent issuances per user) are the two hot paths.
CREATE INDEX IF NOT EXISTS idx_verification_tokens_hash_type
    ON verification_tokens (token_hash, type);

CREATE INDEX IF NOT EXISTS idx_verification_tokens_user_type_created
    ON verification_tokens (user_id, type, created_at);
