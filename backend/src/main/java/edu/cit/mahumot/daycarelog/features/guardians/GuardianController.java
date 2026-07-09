package edu.cit.mahumot.daycarelog.features.guardians;

import edu.cit.mahumot.daycarelog.common.email.EmailValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

// Admin/Staff only (enforced in SecurityConfig) - manages guardian contact info and
// optional parent portal account linking for a child.
@RestController
@RequestMapping("/api/children/{childId}/guardians")
public class GuardianController {

    private final GuardianService guardianService;

    public GuardianController(GuardianService guardianService) {
        this.guardianService = guardianService;
    }

    @GetMapping
    public List<Guardian> list(@PathVariable Long childId) {
        return guardianService.findByChild(childId);
    }

    @PostMapping
    public ResponseEntity<?> add(@PathVariable Long childId, @RequestBody GuardianRequest req) {
        try {
            GuardianService.CreatedGuardian created = guardianService.addGuardian(childId, req);
            return ResponseEntity.ok(Map.of(
                    "guardian", created.guardian(),
                    "tempPassword", created.tempPassword() == null ? "" : created.tempPassword()
            ));
        } catch (EmailValidationException e) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body(Map.of("message", e.getMessage(), "code", e.getCode()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{guardianId}")
    public ResponseEntity<?> remove(@PathVariable Long childId, @PathVariable Long guardianId) {
        try {
            guardianService.removeGuardian(childId, guardianId);
            return ResponseEntity.ok(Map.of("message", "Deleted"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
