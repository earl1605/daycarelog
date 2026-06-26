package com.daycarelog.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class HealthRecordRequest {
    private Long childId;
    private LocalDate measurementDate;
    private BigDecimal weightKg;
    private BigDecimal heightCm;
    private String nutritionalStatus;
    private String remarks;
}
