package edu.cit.mahumot.daycarelog.controller;

import edu.cit.mahumot.daycarelog.dto.GuardianAccountResponse;
import edu.cit.mahumot.daycarelog.service.GuardianService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

// Admin/Staff only (enforced in SecurityConfig) - the Parent/Guardian portal-account
// directory, as opposed to GuardianController which manages one child's contacts.
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
