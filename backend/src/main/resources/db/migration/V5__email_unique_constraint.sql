-- Duplicate-email detection on registration for DaycareLog
--
-- Reference migration only (see V1__email_verification.sql for why): this
-- project uses Hibernate ddl-auto=update, not Flyway/Liquibase.
--
-- Nothing to add here, technically - User.email already has
-- @Column(nullable = false, unique = true), so Hibernate created this
-- constraint automatically the first time that annotation was deployed.
-- This file documents/verifies what already exists rather than changing
-- anything, and records the case-insensitivity decision.
--
-- Case-insensitivity approach chosen: store-lowercased-on-write, not a
-- functional UNIQUE index on LOWER(email). EmailFormatValidator.normalizeAndValidate()
-- already lowercases every email before it's compared or persisted
-- (registration, guardian account linking, staff account creation all go
-- through it), so a plain UNIQUE constraint on the raw column is sufficient -
-- two rows can never differ only by case in the first place. A LOWER(email)
-- functional index would be redundant defense-in-depth, not a requirement.

-- Verify the constraint exists (Supabase/Postgres). Expect one row named
-- "users_email_key" (Hibernate's default naming for `unique = true`):
--   SELECT conname, pg_get_constraintdef(oid)
--   FROM pg_constraint
--   WHERE conrelid = 'users'::regclass AND contype = 'u';

-- If it's ever missing for any reason, this recreates it. Postgres has no
-- "ADD CONSTRAINT IF NOT EXISTS", so guard it explicitly:
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conrelid = 'users'::regclass AND contype = 'u'
          AND pg_get_constraintdef(oid) = 'UNIQUE (email)'
    ) THEN
        ALTER TABLE users ADD CONSTRAINT users_email_key UNIQUE (email);
    END IF;
END $$;
