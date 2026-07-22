package edu.cit.mahumot.daycarelog.features.health;

import edu.cit.mahumot.daycarelog.common.security.JwtUtil;
import edu.cit.mahumot.daycarelog.features.guardians.GuardianService;
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
    public ResponseEntity<?> delete(@PathVariable Long id, @RequestHeader("Authorization") String authHeader) {
        try {
            Long userId = jwtUtil.extractUserId(authHeader.substring(7));
            healthRecordService.delete(id, userId);
            return ResponseEntity.ok(Map.of("message", "Moved to Recycle Bin"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/trash")
    public List<HealthRecord> getTrash() {
        return healthRecordService.findTrashed();
    }

    @PutMapping("/{id}/restore")
    public ResponseEntity<?> restore(@PathVariable Long id) {
        try {
            healthRecordService.restore(id);
            return ResponseEntity.ok(Map.of("message", "Restored"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}/permanent")
    public ResponseEntity<?> permanentlyDelete(@PathVariable Long id) {
        try {
            healthRecordService.permanentlyDelete(id);
            return ResponseEntity.ok(Map.of("message", "Permanently deleted"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
