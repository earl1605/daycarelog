package edu.cit.mahumot.daycarelog.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "attendance", uniqueConstraints = @UniqueConstraint(columnNames = {"child_id", "date"}))
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "child_id", nullable = false)
    private Long childId;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private String status;

    @Column(name = "time_in")
    private LocalTime timeIn;

    @Column(name = "time_out")
    private LocalTime timeOut;

    @Column(name = "recorded_by")
    private Long recordedBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public Attendance() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getChildId() { return childId; }
    public void setChildId(Long childId) { this.childId = childId; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalTime getTimeIn() { return timeIn; }
    public void setTimeIn(LocalTime timeIn) { this.timeIn = timeIn; }

    public LocalTime getTimeOut() { return timeOut; }
    public void setTimeOut(LocalTime timeOut) { this.timeOut = timeOut; }

    public Long getRecordedBy() { return recordedBy; }
    public void setRecordedBy(Long recordedBy) { this.recordedBy = recordedBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long childId;
        private LocalDate date;
        private String status;
        private LocalTime timeIn;
        private LocalTime timeOut;
        private Long recordedBy;

        public Builder childId(Long v) { this.childId = v; return this; }
        public Builder date(LocalDate v) { this.date = v; return this; }
        public Builder status(String v) { this.status = v; return this; }
        public Builder timeIn(LocalTime v) { this.timeIn = v; return this; }
        public Builder timeOut(LocalTime v) { this.timeOut = v; return this; }
        public Builder recordedBy(Long v) { this.recordedBy = v; return this; }

        public Attendance build() {
            Attendance a = new Attendance();
            a.childId = this.childId;
            a.date = this.date;
            a.status = this.status;
            a.timeIn = this.timeIn;
            a.timeOut = this.timeOut;
            a.recordedBy = this.recordedBy;
            return a;
        }
    }
}
