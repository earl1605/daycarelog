package com.daycarelog.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class AttendanceRequest {
    private Long childId;
    private LocalDate date;
    private String status;
    private LocalTime timeIn;
    private LocalTime timeOut;
}
