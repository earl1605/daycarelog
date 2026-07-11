-- Soft delete for health_records and immunizations, backing a Recycle Bin
-- (admin-only restore / permanent-delete) instead of losing data on an
-- accidental delete.
--
-- Reference migration only -- see V1__email_verification.sql: Hibernate
-- ddl-auto=update adds the deleted_at columns automatically on next boot,
-- once the updated entities are deployed.
ALTER TABLE health_records
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;

ALTER TABLE immunizations
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;

-- The following two statements ARE something you need to run by hand --
-- ddl-auto=update never drops or replaces constraints/indexes, so if
-- V3 was already applied (or the immunizations table was already created
-- by an earlier deploy), the old table-wide UNIQUE(child_id, vaccine_name,
-- dose_number) constraint is still sitting there and will now block
-- re-recording a dose after its old row was soft-deleted, since that old
-- row still physically occupies the combination.
--
-- Default Postgres naming for an unnamed UNIQUE(...) table constraint is
-- <table>_<col1>_<col2>_..._key -- adjust the name below if yours differs
-- (check with: SELECT conname FROM pg_constraint WHERE conrelid =
-- 'immunizations'::regclass AND contype = 'u';)
ALTER TABLE immunizations
    DROP CONSTRAINT IF EXISTS immunizations_child_id_vaccine_name_dose_number_key;

-- Replaces it with a partial unique index that only applies to *active*
-- (not soft-deleted) rows -- the DB-level equivalent of the check now done
-- in ImmunizationService.create(). Application code already enforces this;
-- this index is defense-in-depth against races/direct DB writes.
CREATE UNIQUE INDEX IF NOT EXISTS ux_immunizations_active_dose
    ON immunizations (child_id, vaccine_name, dose_number)
    WHERE deleted_at IS NULL;
