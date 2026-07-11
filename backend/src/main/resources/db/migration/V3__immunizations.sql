-- Immunization tracking (DOH EPI schedule)
--
-- Reference migration only -- see V1__email_verification.sql: Hibernate
-- ddl-auto=update creates this table and its plain columns automatically
-- from the Immunization entity on next boot. It will NOT create the
-- REFERENCES/foreign-key constraint below on its own, since child_id is a
-- plain Long column here, same as every other child_id column in this
-- codebase (Attendance, HealthRecord, Guardian) -- none of them use a JPA
-- relation. The unique constraint on (child_id, vaccine_name, dose_number)
-- IS entity-managed (see Immunization's @Table uniqueConstraints, same
-- pattern as Attendance's one-record-per-child-per-day rule) and will be
-- created automatically. Run this by hand in Supabase if you want the
-- FK/index to exist before a deploy.
CREATE TABLE IF NOT EXISTS immunizations (
    id               BIGSERIAL PRIMARY KEY,
    child_id         BIGINT NOT NULL REFERENCES children(id) ON DELETE CASCADE,
    vaccine_name     VARCHAR(100) NOT NULL,
    dose_number      INTEGER NOT NULL,
    date_given       DATE NOT NULL,
    administered_by  VARCHAR(150),
    notes            TEXT,
    created_at       TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (child_id, vaccine_name, dose_number)
);

CREATE INDEX IF NOT EXISTS idx_immunizations_child_id ON immunizations (child_id);
