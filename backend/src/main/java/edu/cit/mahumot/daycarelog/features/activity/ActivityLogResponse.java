package edu.cit.mahumot.daycarelog.features.activity;

import java.time.LocalDateTime;

/**
 * Read-shape for ActivityLog, styled after AuthResponse.UserDto (the one
 * existing DTO precedent in this codebase). A DTO is used here - unlike
 * every other list endpoint, which returns entities directly - because the
 * raw entity only carries userId, not a display name; actorName is resolved
 * by the service at read time (see ActivityLogService.toResponse), never
 * stored, so it stays correct even for a since-deleted user.
 */
public class ActivityLogResponse {
    private Long id;
    private String action;
    private String entityType;
    private Long entityId;
    private Long childId;
    private String description;
    private String actorName;
    private LocalDateTime createdAt;

    public ActivityLogResponse(Long id, String action, String entityType, Long entityId, Long childId,
                                String description, String actorName, LocalDateTime createdAt) {
        this.id = id;
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.childId = childId;
        this.description = description;
        this.actorName = actorName;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public String getAction() { return action; }
    public String getEntityType() { return entityType; }
    public Long getEntityId() { return entityId; }
    public Long getChildId() { return childId; }
    public String getDescription() { return description; }
    public String getActorName() { return actorName; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
