package edu.cit.mahumot.daycarelog.features.activity;

/**
 * Allowed values for ActivityLog.action. Plain String constants, not a Java
 * enum - no entity in this codebase uses @Enumerated (User.role,
 * Child.enrollmentStatus, Attendance.status are all plain String columns),
 * so this matches that existing style rather than introducing enums for the
 * first time. The activity_logs.action column itself stays unconstrained
 * VARCHAR (see V6__activity_log.sql) - these constants are what actually
 * enforce which values are valid, at the call sites in each service.
 */
public final class ActivityActions {
    private ActivityActions() {}

    public static final String CHILD_CREATED = "CHILD_CREATED";
    public static final String CHILD_UPDATED = "CHILD_UPDATED";
    public static final String CHILD_DELETED = "CHILD_DELETED";

    // Attendance has no separate check-in/check-out event in this codebase -
    // AttendanceService.upsert() writes one status (Present/Absent/Late/
    // Excused) per child per day. One action name matches that model.
    public static final String ATTENDANCE_RECORDED = "ATTENDANCE_RECORDED";

    public static final String HEALTH_RECORD_CREATED = "HEALTH_RECORD_CREATED";
    public static final String HEALTH_RECORD_DELETED = "HEALTH_RECORD_DELETED";

    public static final String USER_CREATED = "USER_CREATED";
    public static final String USER_ROLE_CHANGED = "USER_ROLE_CHANGED";
    public static final String USER_DEACTIVATED = "USER_DEACTIVATED";
    public static final String USER_REACTIVATED = "USER_REACTIVATED";
    public static final String USER_DELETED = "USER_DELETED";

    public static final String GUARDIAN_CREATED = "GUARDIAN_CREATED";
    public static final String GUARDIAN_DELETED = "GUARDIAN_DELETED";
}
