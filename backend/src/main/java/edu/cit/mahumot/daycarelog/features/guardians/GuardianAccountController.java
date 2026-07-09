package edu.cit.mahumot.daycarelog.features.guardians;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/guardians")
public class GuardianAccountController {

    private final GuardianService guardianService;

    public GuardianAccountController(GuardianService guardianService) {
        this.guardianService = guardianService;
    }

    @GetMapping
    public List<GuardianAccountResponse> list() {
        return guardianService.findAllAccounts();
    }

    @DeleteMapping("/user/{userId}")
    public ResponseEntity<?> removeAccount(@PathVariable Long userId) {
        try {
            guardianService.removeAllForUser(userId);
            return ResponseEntity.ok(Map.of("message", "Removed"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
