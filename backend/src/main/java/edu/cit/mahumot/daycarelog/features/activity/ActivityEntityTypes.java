package edu.cit.mahumot.daycarelog.features.activity;

/** Allowed values for ActivityLog.entityType. See ActivityActions for why these are plain constants, not an enum. */
public final class ActivityEntityTypes {
    private ActivityEntityTypes() {}

    public static final String CHILD = "CHILD";
    public static final String ATTENDANCE = "ATTENDANCE";
    public static final String HEALTH_RECORD = "HEALTH_RECORD";
    public static final String USER = "USER";
    public static final String GUARDIAN = "GUARDIAN";
}
