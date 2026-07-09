package edu.cit.mahumot.daycarelog.features.children;

import edu.cit.mahumot.daycarelog.common.security.JwtUtil;
import edu.cit.mahumot.daycarelog.features.guardians.GuardianService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/children")
public class ChildController {

    private final ChildService childService;
    private final GuardianService guardianService;
    private final JwtUtil jwtUtil;

    public ChildController(ChildService childService, GuardianService guardianService, JwtUtil jwtUtil) {
        this.childService = childService;
        this.guardianService = guardianService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping
    public List<Child> getAll() {
        return childService.findAll();
    }

    @GetMapping("/mine")
    public ResponseEntity<?> getMine(@RequestHeader("Authorization") String authHeader) {
        Long userId = jwtUtil.extractUserId(authHeader.substring(7));
        List<Long> childIds = guardianService.findChildIdsForUser(userId);
        return ResponseEntity.ok(childService.findByIds(childIds));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(childService.findById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody ChildRequest req,
                                    @RequestHeader("Authorization") String authHeader) {
        try {
            Long userId = jwtUtil.extractUserId(authHeader.substring(7));
            return ResponseEntity.ok(childService.create(req, userId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody ChildRequest req) {
        try {
            return ResponseEntity.ok(childService.update(id, req));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        childService.delete(id);
        return ResponseEntity.ok(Map.of("message", "Deleted"));
    }
}