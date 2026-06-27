package com.daycarelog.controller;

import com.daycarelog.dto.HealthRecordRequest;
import com.daycarelog.model.HealthRecord;
import com.daycarelog.security.JwtUtil;
import com.daycarelog.service.HealthRecordService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/health-records")
public class HealthRecordController {

    private final HealthRecordService healthRecordService;
    private final JwtUtil jwtUtil;

    public HealthRecordController(HealthRecordService healthRecordService, JwtUtil jwtUtil) {
        this.healthRecordService = healthRecordService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping
    public List<HealthRecord> getAll() {
        return healthRecordService.findAll();
    }

    @GetMapping("/child/{childId}")
    public List<HealthRecord> getByChild(@PathVariable Long childId) {
        return healthRecordService.findByChild(childId);
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody HealthRecordRequest req,
                                    @RequestHeader("Authorization") String authHeader) {
        try {
            Long userId = jwtUtil.extractUserId(authHeader.substring(7));
            return ResponseEntity.ok(healthRecordService.create(req, userId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
