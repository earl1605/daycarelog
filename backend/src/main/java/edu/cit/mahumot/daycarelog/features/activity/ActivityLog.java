package edu.cit.mahumot.daycarelog.features.activity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "activity_logs", indexes = {
        @Index(name = "idx_activity_logs_created_at", columnList = "created_at"),
        @Index(name = "idx_activity_logs_entity", columnList = "entity_type, entity_id"),
        @Index(name = "idx_activity_logs_user", columnList = "user_id"),
        @Index(name = "idx_activity_logs_child", columnList = "child_id")
})
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false, length = 50)
    private String action;

    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType;

    @Column(name = "entity_id")
    private Long entityId;

    @Column(name = "child_id")
    private Long childId;

    @Column(columnDefinition = "TEXT")
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public ActivityLog() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }

    public Long getEntityId() { return entityId; }
    public void setEntityId(Long entityId) { this.entityId = entityId; }

    public Long getChildId() { return childId; }
    public void setChildId(Long childId) { this.childId = childId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long userId;
        private String action;
        private String entityType;
        private Long entityId;
        private Long childId;
        private String description;

        public Builder userId(Long v) { this.userId = v; return this; }
        public Builder action(String v) { this.action = v; return this; }
        public Builder entityType(String v) { this.entityType = v; return this; }
        public Builder entityId(Long v) { this.entityId = v; return this; }
        public Builder childId(Long v) { this.childId = v; return this; }
        public Builder description(String v) { this.description = v; return this; }

        public ActivityLog build() {
            ActivityLog a = new ActivityLog();
            a.userId = this.userId;
            a.action = this.action;
            a.entityType = this.entityType;
            a.entityId = this.entityId;
            a.childId = this.childId;
            a.description = this.description;
            return a;
        }
    }
}
