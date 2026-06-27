package com.daycarelog.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "health_records")
public class HealthRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "child_id", nullable = false)
    private Long childId;

    @Column(name = "measurement_date", nullable = false)
    private LocalDate measurementDate;

    @Column(name = "weight_kg", precision = 5, scale = 2)
    private BigDecimal weightKg;

    @Column(name = "height_cm", precision = 5, scale = 2)
    private BigDecimal heightCm;

    @Column(name = "nutritional_status")
    private String nutritionalStatus;

    private String remarks;

    @Column(name = "recorded_by")
    private Long recordedBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public HealthRecord() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getChildId() { return childId; }
    public void setChildId(Long childId) { this.childId = childId; }

    public LocalDate getMeasurementDate() { return measurementDate; }
    public void setMeasurementDate(LocalDate measurementDate) { this.measurementDate = measurementDate; }

    public BigDecimal getWeightKg() { return weightKg; }
    public void setWeightKg(BigDecimal weightKg) { this.weightKg = weightKg; }

    public BigDecimal getHeightCm() { return heightCm; }
    public void setHeightCm(BigDecimal heightCm) { this.heightCm = heightCm; }

    public String getNutritionalStatus() { return nutritionalStatus; }
    public void setNutritionalStatus(String nutritionalStatus) { this.nutritionalStatus = nutritionalStatus; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public Long getRecordedBy() { return recordedBy; }
    public void setRecordedBy(Long recordedBy) { this.recordedBy = recordedBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long childId;
        private LocalDate measurementDate;
        private BigDecimal weightKg;
        private BigDecimal heightCm;
        private String nutritionalStatus;
        private String remarks;
        private Long recordedBy;

        public Builder childId(Long v) { this.childId = v; return this; }
        public Builder measurementDate(LocalDate v) { this.measurementDate = v; return this; }
        public Builder weightKg(BigDecimal v) { this.weightKg = v; return this; }
        public Builder heightCm(BigDecimal v) { this.heightCm = v; return this; }
        public Builder nutritionalStatus(String v) { this.nutritionalStatus = v; return this; }
        public Builder remarks(String v) { this.remarks = v; return this; }
        public Builder recordedBy(Long v) { this.recordedBy = v; return this; }

        public HealthRecord build() {
            HealthRecord r = new HealthRecord();
            r.childId = this.childId;
            r.measurementDate = this.measurementDate;
            r.weightKg = this.weightKg;
            r.heightCm = this.heightCm;
            r.nutritionalStatus = this.nutritionalStatus;
            r.remarks = this.remarks;
            r.recordedBy = this.recordedBy;
            return r;
        }
    }
}
