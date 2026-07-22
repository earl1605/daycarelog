package edu.cit.mahumot.daycarelog.features.guardians;

import edu.cit.mahumot.daycarelog.common.email.EmailValidationException;
import edu.cit.mahumot.daycarelog.common.security.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/children/{childId}/guardians")
public class GuardianController {

    private final GuardianService guardianService;
    private final JwtUtil jwtUtil;

    public GuardianController(GuardianService guardianService, JwtUtil jwtUtil) {
        this.guardianService = guardianService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping
    public List<Guardian> list(@PathVariable Long childId) {
        return guardianService.findByChild(childId);
    }

    @PostMapping
    public ResponseEntity<?> add(@PathVariable Long childId, @RequestBody GuardianRequest req,
                                 @RequestHeader("Authorization") String authHeader) {
        try {
            Long requesterId = jwtUtil.extractUserId(authHeader.substring(7));
            GuardianService.CreatedGuardian created = guardianService.addGuardian(childId, req, requesterId);
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
    public ResponseEntity<?> remove(@PathVariable Long childId, @PathVariable Long guardianId,
                                    @RequestHeader("Authorization") String authHeader) {
        try {
            Long requesterId = jwtUtil.extractUserId(authHeader.substring(7));
            guardianService.removeGuardian(childId, guardianId, requesterId);
            return ResponseEntity.ok(Map.of("message", "Deleted"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
