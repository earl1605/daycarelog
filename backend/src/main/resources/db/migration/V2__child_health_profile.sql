-- Child health profile fields (allergies, medical conditions, blood type)
--
-- Reference migration only -- see V1__email_verification.sql: this project
-- runs on Hibernate ddl-auto=update, so the ALTER TABLE below applies
-- automatically the next time the backend boots against Supabase, once the
-- updated Child entity is deployed. This file documents that DDL; run it by
-- hand in the Supabase SQL editor if you want the columns to exist before
-- the deploy.

-- These are slowly-changing facts about the child, not a dated checkup
-- measurement, so they live on `children` rather than `health_records`.
ALTER TABLE children
    ADD COLUMN IF NOT EXISTS allergies           TEXT,
    ADD COLUMN IF NOT EXISTS medical_conditions   TEXT,
    ADD COLUMN IF NOT EXISTS blood_type           VARCHAR(5);

-- Optional, not created by Hibernate: existing health_records rows may hold
-- the old client-computed nutritional_status labels ("Normal", "Severely
-- Underweight", ...) from before HealthRecordService started computing this
-- value itself. Run this once so historical records use the same
-- NORMAL / UNDERWEIGHT / SEVERELY_UNDERWEIGHT / OVERWEIGHT vocabulary as
-- new ones -- otherwise old rows won't match the frontend's status badge
-- lookup. Safe to run more than once.
UPDATE health_records
SET nutritional_status = UPPER(REPLACE(TRIM(nutritional_status), ' ', '_'))
WHERE nutritional_status IS NOT NULL;
