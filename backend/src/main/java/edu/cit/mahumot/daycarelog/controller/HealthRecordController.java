package edu.cit.mahumot.daycarelog.controller;

import edu.cit.mahumot.daycarelog.dto.HealthRecordRequest;
import edu.cit.mahumot.daycarelog.model.HealthRecord;
import edu.cit.mahumot.daycarelog.security.JwtUtil;
import edu.cit.mahumot.daycarelog.service.HealthRecordService;
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
