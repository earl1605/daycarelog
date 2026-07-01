package edu.cit.mahumot.daycarelog.controller;

import edu.cit.mahumot.daycarelog.dto.*;
import edu.cit.mahumot.daycarelog.model.User;
import edu.cit.mahumot.daycarelog.security.JwtUtil;
import edu.cit.mahumot.daycarelog.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    public UserController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping
    public List<User> getAll() {
        return userService.findAll();
    }

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody CreateUserRequest req) {
        try {
            UserService.CreatedUser created = userService.createUser(req);
            return ResponseEntity.ok(Map.of("user", created.user(), "tempPassword", created.tempPassword()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}/role")
    public ResponseEntity<?> updateRole(@PathVariable Long id, @RequestBody UserRoleRequest req) {
        try {
            return ResponseEntity.ok(userService.updateRole(id, req.getRole()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}/deactivate")
    public ResponseEntity<?> deactivate(@PathVariable Long id, @RequestHeader("Authorization") String authHeader) {
        try {
            Long requesterId = jwtUtil.extractUserId(authHeader.substring(7));
            return ResponseEntity.ok(userService.deactivateUser(id, requesterId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}/reactivate")
    public ResponseEntity<?> reactivate(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(userService.reactivateUser(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}/reset-password")
    public ResponseEntity<?> resetPassword(@PathVariable Long id) {
        try {
            String tempPassword = userService.resetPassword(id);
            return ResponseEntity.ok(Map.of("tempPassword", tempPassword));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProfile(@PathVariable Long id,
                                           @RequestBody UpdateProfileRequest req,
                                           @RequestHeader("Authorization") String authHeader) {
        try {
            Long requesterId = jwtUtil.extractUserId(authHeader.substring(7));
            if (!requesterId.equals(id)) {
                return ResponseEntity.status(403).body(Map.of("message", "Forbidden"));
            }
            return ResponseEntity.ok(userService.updateProfile(id, req));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id,
                                        @RequestHeader("Authorization") String authHeader) {
        try {
            Long requesterId = jwtUtil.extractUserId(authHeader.substring(7));
            userService.deleteUser(id, requesterId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
