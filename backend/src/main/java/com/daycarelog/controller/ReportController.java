package com.daycarelog.controller;

import com.daycarelog.model.Attendance;
import com.daycarelog.model.Child;
import com.daycarelog.model.HealthRecord;
import com.daycarelog.repository.AttendanceRepository;
import com.daycarelog.repository.ChildRepository;
import com.daycarelog.repository.HealthRecordRepository;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ChildRepository childRepository;
    private final AttendanceRepository attendanceRepository;
    private final HealthRecordRepository healthRecordRepository;

    public ReportController(ChildRepository childRepository,
                            AttendanceRepository attendanceRepository,
                            HealthRecordRepository healthRecordRepository) {
        this.childRepository = childRepository;
        this.attendanceRepository = attendanceRepository;
        this.healthRecordRepository = healthRecordRepository;
    }

    @GetMapping("/monthly")
    public Map<String, Object> monthly(@RequestParam String month) {
        LocalDate start = LocalDate.parse(month + "-01");
        LocalDate end   = start.withDayOfMonth(start.lengthOfMonth());

        List<Child>        children = childRepository.findByEnrollmentStatus("active");
        List<Attendance>   att      = attendanceRepository.findByDateBetween(start, end);
        List<HealthRecord> health   = healthRecordRepository.findByMeasurementDateBetween(start, end);

        int total   = children.size();
        int present = (int) att.stream().filter(a -> "present".equals(a.getStatus())).count();
        int absent  = (int) att.stream().filter(a -> "absent".equals(a.getStatus())).count();
        long days   = att.stream().map(Attendance::getDate).distinct().count();
        int rate    = (total > 0 && days > 0) ? (int) Math.round((double) present / (total * days) * 100) : 0;

        Map<Long, HealthRecord> latestHealth = new HashMap<>();
        health.forEach(r -> latestHealth.merge(r.getChildId(), r,
                (a, b) -> a.getMeasurementDate().isAfter(b.getMeasurementDate()) ? a : b));

        Map<String, Integer> statusCounts = new LinkedHashMap<>();
        statusCounts.put("Normal", 0);
        statusCounts.put("Underweight", 0);
        statusCounts.put("Severely Underweight", 0);
        statusCounts.put("Overweight", 0);
        statusCounts.put("Unknown", 0);
        children.forEach(c -> {
            HealthRecord r = latestHealth.get(c.getId());
            String s = (r != null && r.getNutritionalStatus() != null) ? r.getNutritionalStatus() : "Unknown";
            statusCounts.merge(s, 1, Integer::sum);
        });

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("total",             total);
        result.put("presentCount",      present);
        result.put("absentCount",       absent);
        result.put("schoolDays",        days);
        result.put("attendanceRate",    rate);
        result.put("nutritionalStatus", statusCounts);
        result.put("children",          children);
        return result;
    }
}
