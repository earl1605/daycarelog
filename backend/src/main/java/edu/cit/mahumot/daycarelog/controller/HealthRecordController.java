package edu.cit.mahumot.daycarelog.controller;

import edu.cit.mahumot.daycarelog.dto.HealthRecordRequest;
import edu.cit.mahumot.daycarelog.model.HealthRecord;
import edu.cit.mahumot.daycarelog.security.JwtUtil;
import edu.cit.mahumot.daycarelog.service.GuardianService;
import edu.cit.mahumot.daycarelog.service.HealthRecordService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/health-records")
public class HealthRecordController {

    private final HealthRecordService healthRecordService;
    private final GuardianService guardianService;
    private final JwtUtil jwtUtil;

    public HealthRecordController(HealthRecordService healthRecordService, GuardianService guardianService, JwtUtil jwtUtil) {
        this.healthRecordService = healthRecordService;
        this.guardianService = guardianService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping
    public List<HealthRecord> getAll() {
        return healthRecordService.findAll();
    }

    // Parent-facing: all health records for the caller's own linked children,
    // resolved server-side from the JWT rather than an accepted child ID.
    @GetMapping("/mine")
    public ResponseEntity<?> getMine(@RequestHeader("Authorization") String authHeader) {
        Long userId = jwtUtil.extractUserId(authHeader.substring(7));
        List<Long> childIds = guardianService.findChildIdsForUser(userId);
        return ResponseEntity.ok(healthRecordService.findByChildIds(childIds));
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

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            healthRecordService.delete(id);
            return ResponseEntity.ok(Map.of("message", "Deleted"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
