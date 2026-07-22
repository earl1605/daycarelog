-- Activity History / Audit Log for DaycareLog
--
-- Reference migration only (see V1__email_verification.sql for why): this
-- project uses Hibernate ddl-auto=update, not Flyway/Liquibase. Hibernate
-- will apply the equivalent DDL automatically on next boot from the
-- ActivityLog entity (features/activity/ActivityLog.java), including the
-- three indexes below via its @Table(indexes = ...) declaration. This file
-- documents exactly what that DDL is, and can be run by hand against
-- Supabase if you want the schema to exist before the app's first boot.
--
-- ON DELETE SET NULL (not CASCADE) on user_id is deliberate: deleting a user
-- must never erase the audit trail of what that user did. A log with a null
-- user_id represents an action by a since-deleted account.
--
-- action / entity_type are intentionally free-form VARCHAR, not CHECK-
-- constrained enums - see features/activity/ActivityActions.java and
-- ActivityEntityTypes.java for the allowed values enforced in application
-- code. Keeping the DB column unconstrained means adding a new action type
-- later never requires a migration.
--
-- child_id is a deliberate addition beyond the originally proposed schema:
-- an attendance/health-record log's entity_id is that record's OWN id, not
-- the child's, so a child's History tab can't be answered by
-- "entity_type='CHILD' AND entity_id=X" alone. child_id is set whenever a
-- log relates to a child at all (CHILD actions: child_id = entity_id;
-- ATTENDANCE/HEALTH_RECORD/GUARDIAN actions: child_id = that record's
-- owning child), making /api/children/{id}/history a single-column filter
-- instead of a cross-table union query. Null for USER actions, which have
-- no associated child.

CREATE TABLE IF NOT EXISTS activity_logs (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT REFERENCES users(id) ON DELETE SET NULL,
    action      VARCHAR(50) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id   BIGINT,
    child_id    BIGINT REFERENCES children(id) ON DELETE SET NULL,
    description TEXT,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Recent-activity feed (Dashboard widget, full History page default sort).
CREATE INDEX IF NOT EXISTS idx_activity_logs_created_at
    ON activity_logs (created_at DESC);

-- Per-entity timeline lookups.
CREATE INDEX IF NOT EXISTS idx_activity_logs_entity
    ON activity_logs (entity_type, entity_id);

-- "What has this user done" lookups / the userId filter on the History page.
CREATE INDEX IF NOT EXISTS idx_activity_logs_user
    ON activity_logs (user_id);

-- A child's History tab.
CREATE INDEX IF NOT EXISTS idx_activity_logs_child
    ON activity_logs (child_id);
