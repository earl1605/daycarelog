package com.daycarelog.controller;

import com.daycarelog.dto.*;
import com.daycarelog.model.User;
import com.daycarelog.security.JwtUtil;
import com.daycarelog.service.UserService;
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

    @PutMapping("/{id}/role")
    public ResponseEntity<?> updateRole(@PathVariable Long id, @RequestBody UserRoleRequest req) {
        try {
            return ResponseEntity.ok(userService.updateRole(id, req.getRole()));
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
