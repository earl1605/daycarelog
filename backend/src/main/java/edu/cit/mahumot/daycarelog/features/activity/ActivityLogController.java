package edu.cit.mahumot.daycarelog.features.activity;

import edu.cit.mahumot.daycarelog.common.security.JwtUtil;
import edu.cit.mahumot.daycarelog.features.guardians.GuardianService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
public class ActivityLogController {

    private final ActivityLogService activityLogService;
    private final GuardianService guardianService;
    private final JwtUtil jwtUtil;

    public ActivityLogController(ActivityLogService activityLogService, GuardianService guardianService, JwtUtil jwtUtil) {
        this.activityLogService = activityLogService;
        this.guardianService = guardianService;
        this.jwtUtil = jwtUtil;
    }

    // Temporary diagnostic endpoint - trivial, zero-parameter, to isolate
    // whether the bare "/api/activity-logs" 403 is about this specific path
    // string vs. something about search()'s parameter list. Remove once the
    // root cause of the 403 on GET /api/activity-logs is found.
    @GetMapping("/api/activity-logs-ping")
    public String ping() {
        return "ok";
    }

    // Second diagnostic: a path with ZERO textual relation to "activity-logs",
    // to test whether ANY brand-new bare top-level path added in this same
    // batch has this problem, or whether it's specific to the activity-logs
    // prefix somehow colliding with the sibling /recent rule.
    @GetMapping("/api/zzz-diagnostic")
    public String zzzDiagnostic() {
        return "ok";
    }

    // ADMIN only (see SecurityConfig) - the full filterable audit log.
    @GetMapping("/api/activity-logs")
    public Page<ActivityLogResponse> search(
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return activityLogService.search(action, entityType, userId, from, to, PageRequest.of(page, size));
    }

    // ADMIN and STAFF (see SecurityConfig) - unscoped, same as every other
    // operational endpoint (children/attendance/health-records) that STAFF
    // already has full system-wide visibility into. Only the filterable
    // /api/activity-logs above is Admin-only.
    @GetMapping("/api/activity-logs/recent")
    public Page<ActivityLogResponse> recent(@RequestParam(defaultValue = "10") int size) {
        return activityLogService.findRecent(PageRequest.of(0, size));
    }

    // ADMIN/STAFF: any child. PARENT: only a child they're linked to as a
    // guardian - reuses GuardianService.isGuardianOfChild, the same
    // mechanism the codebase already has for this exact check.
    @GetMapping("/api/children/{id}/history")
    public ResponseEntity<?> childHistory(@PathVariable Long id,
                                          @RequestHeader("Authorization") String authHeader,
                                          @RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "20") int size) {
        String token = authHeader.substring(7);
        Long userId = jwtUtil.extractUserId(token);
        String role = jwtUtil.extractRole(token);
        if ("parent".equals(role) && !guardianService.isGuardianOfChild(userId, id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Forbidden"));
        }
        return ResponseEntity.ok(activityLogService.findByChild(id, PageRequest.of(page, size)));
    }
}
