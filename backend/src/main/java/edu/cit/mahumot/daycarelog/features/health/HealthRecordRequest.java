package edu.cit.mahumot.daycarelog.features.health;

import java.math.BigDecimal;
import java.time.LocalDate;

public class HealthRecordRequest {
    private Long childId;
    private LocalDate measurementDate;
    private BigDecimal weightKg;
    private BigDecimal heightCm;
    private String nutritionalStatus;
    private String remarks;

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
}
