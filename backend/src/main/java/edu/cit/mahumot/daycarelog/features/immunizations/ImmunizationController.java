package edu.cit.mahumot.daycarelog.features.immunizations;

import edu.cit.mahumot.daycarelog.common.security.JwtUtil;
import edu.cit.mahumot.daycarelog.features.guardians.GuardianService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/immunizations")
public class ImmunizationController {

    private final ImmunizationService immunizationService;
    private final GuardianService guardianService;
    private final JwtUtil jwtUtil;

    public ImmunizationController(ImmunizationService immunizationService, GuardianService guardianService, JwtUtil jwtUtil) {
        this.immunizationService = immunizationService;
        this.guardianService = guardianService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping
    public List<Immunization> getAll() {
        return immunizationService.findAll();
    }

    @GetMapping("/schedule")
    public List<EpiVaccine> getSchedule() {
        return EpiVaccineSchedule.ALL;
    }

    @GetMapping("/mine")
    public ResponseEntity<?> getMine(@RequestHeader("Authorization") String authHeader) {
        Long userId = jwtUtil.extractUserId(authHeader.substring(7));
        List<Long> childIds = guardianService.findChildIdsForUser(userId);
        return ResponseEntity.ok(immunizationService.findByChildIds(childIds));
    }

    @GetMapping("/child/{childId}")
    public List<Immunization> getByChild(@PathVariable Long childId) {
        return immunizationService.findByChild(childId);
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody ImmunizationRequest req) {
        try {
            return ResponseEntity.ok(immunizationService.create(req));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            immunizationService.delete(id);
            return ResponseEntity.ok(Map.of("message", "Deleted"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
