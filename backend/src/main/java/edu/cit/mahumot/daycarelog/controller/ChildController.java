package edu.cit.mahumot.daycarelog.controller;

import edu.cit.mahumot.daycarelog.dto.ChildRequest;
import edu.cit.mahumot.daycarelog.model.Child;
import edu.cit.mahumot.daycarelog.security.JwtUtil;
import edu.cit.mahumot.daycarelog.service.ChildService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/children")
public class ChildController {

    private final ChildService childService;
    private final JwtUtil jwtUtil;

    public ChildController(ChildService childService, JwtUtil jwtUtil) {
        this.childService = childService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping
    public List<Child> getAll() {
        return childService.findAll();
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