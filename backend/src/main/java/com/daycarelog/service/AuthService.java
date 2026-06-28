package com.daycarelog.service;

import com.daycarelog.dto.*;
import com.daycarelog.model.User;
import com.daycarelog.repository.UserRepository;
import com.daycarelog.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new RuntimeException("Email already in use");
        }
        String role = "staff";
        if (req.getRole() != null) {
            String r = req.getRole().toLowerCase().trim();
            if (r.equals("admin") || r.equals("teacher") || r.equals("staff")) {
                role = r;
            }
        }
        User user = User.builder()
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .firstName(req.getFirstName())
                .lastName(req.getLastName())
                .middleName(req.getMiddleName())
                .suffix(req.getSuffix())
                .role(role)
                .build();
        user = userRepository.save(user);
        return buildResponse(user);
    }

    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }
        return buildResponse(user);
    }

    private AuthResponse buildResponse(User user) {
        String token = jwtUtil.generateToken(user.getEmail(), user.getId(), user.getRole());
        AuthResponse.UserDto dto = new AuthResponse.UserDto(
                user.getId(), user.getEmail(), user.getFullName(),
                user.getFirstName(), user.getLastName(), user.getMiddleName(), user.getSuffix(),
                user.getRole(), user.getProfilePhoto());
        return new AuthResponse(token, dto);
    }
}
