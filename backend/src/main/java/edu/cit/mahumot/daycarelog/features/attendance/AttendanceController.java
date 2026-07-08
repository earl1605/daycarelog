package edu.cit.mahumot.daycarelog.features.attendance;

import edu.cit.mahumot.daycarelog.common.security.JwtUtil;
import edu.cit.mahumot.daycarelog.features.guardians.GuardianService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final GuardianService guardianService;
    private final JwtUtil jwtUtil;

    public AttendanceController(AttendanceService attendanceService, GuardianService guardianService, JwtUtil jwtUtil) {
        this.attendanceService = attendanceService;
        this.guardianService = guardianService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping
    public List<Attendance> getByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return attendanceService.findByDate(date);
    }

    // Parent-facing: all attendance records for the caller's own linked children,
    // resolved server-side from the JWT rather than an accepted child ID.
    @GetMapping("/mine")
    public ResponseEntity<?> getMine(@RequestHeader("Authorization") String authHeader) {
        Long userId = jwtUtil.extractUserId(authHeader.substring(7));
        List<Long> childIds = guardianService.findChildIdsForUser(userId);
        return ResponseEntity.ok(attendanceService.findByChildIds(childIds));
    }

    @GetMapping("/child/{childId}")
    public List<Attendance> getByChild(@PathVariable Long childId) {
        return attendanceService.findByChild(childId);
    }

    @PostMapping("/bulk")
    public ResponseEntity<?> saveBulk(@RequestBody List<AttendanceRequest> requests,
                                      @RequestHeader("Authorization") String authHeader) {
        try {
            Long userId = jwtUtil.extractUserId(authHeader.substring(7));
            return ResponseEntity.ok(attendanceService.bulkUpsert(requests, userId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/range")
    public List<Attendance> getByRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return attendanceService.findByDateRange(start, end);
    }
}
