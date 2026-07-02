package edu.cit.mahumot.daycarelog.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class AttendanceRequest {
    private Long childId;
    private LocalDate date;
    private String status;
    private LocalTime timeIn;
    private LocalTime timeOut;

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
}
