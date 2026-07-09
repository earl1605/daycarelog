package edu.cit.mahumot.daycarelog.features.auth;

import edu.cit.mahumot.daycarelog.common.email.EmailValidationException;
import edu.cit.mahumot.daycarelog.common.security.JwtUtil;
import edu.cit.mahumot.daycarelog.features.users.User;
import edu.cit.mahumot.daycarelog.features.users.UserRepository;
import edu.cit.mahumot.daycarelog.features.verification.VerificationException;
import edu.cit.mahumot.daycarelog.features.verification.VerificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final VerificationService verificationService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public AuthController(AuthService authService, VerificationService verificationService,
                           UserRepository userRepository, JwtUtil jwtUtil) {
        this.authService = authService;
        this.verificationService = verificationService;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        try {
            authService.register(req);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "message", "Registration received. Please check your email to verify your account."));
        } catch (EmailValidationException e) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body(Map.of("message", e.getMessage(), "code", e.getCode()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        try {
            return ResponseEntity.ok(authService.login(req));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestBody VerifyEmailRequest req) {
        try {
            VerificationService.VerifyResult result = (req.getToken() != null && !req.getToken().isBlank())
                    ? verificationService.verifyByToken(req.getToken())
                    : verificationService.verifyByCode(req.getEmail(), req.getCode());
            User user = result.user();
            AuthResponse.UserDto dto = new AuthResponse.UserDto(
                    user.getId(), user.getEmail(), user.getFullName(),
                    user.getFirstName(), user.getLastName(), user.getMiddleName(), user.getSuffix(),
                    user.getRole(), user.getProfilePhoto(), true);
            return ResponseEntity.ok(Map.of(
                    "message", "Email verified successfully.",
                    "token", result.token(),
                    "user", dto));
        } catch (VerificationException e) {
            return errorResponse(e);
        }
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendVerification(@RequestBody ResendVerificationRequest req) {
        try {
            verificationService.resend(req.getEmail());
        } catch (VerificationException e) {
            return errorResponse(e);
        }
        return ResponseEntity.ok(Map.of(
                "message", "If that email needs verification, we've sent a new code and link."));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(@RequestHeader("Authorization") String authHeader) {
        User user = currentUser(authHeader);
        boolean verified = Boolean.TRUE.equals(user.getEmailVerified());
        AuthResponse.UserDto dto = new AuthResponse.UserDto(
                user.getId(), user.getEmail(), user.getFullName(),
                user.getFirstName(), user.getLastName(), user.getMiddleName(), user.getSuffix(),
                user.getRole(), user.getProfilePhoto(), verified);
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestHeader("Authorization") String authHeader) {
        User user = currentUser(authHeader);
        boolean verified = Boolean.TRUE.equals(user.getEmailVerified());
        String token = jwtUtil.generateToken(user.getEmail(), user.getId(), user.getRole(), verified);
        AuthResponse.UserDto dto = new AuthResponse.UserDto(
                user.getId(), user.getEmail(), user.getFullName(),
                user.getFirstName(), user.getLastName(), user.getMiddleName(), user.getSuffix(),
                user.getRole(), user.getProfilePhoto(), verified);
        return ResponseEntity.ok(new AuthResponse(token, dto));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        return ResponseEntity.ok(Map.of("message", "Logged out."));
    }

    private User currentUser(String authHeader) {
        Long userId = jwtUtil.extractUserId(authHeader.substring(7));
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Account not found"));
    }

    private ResponseEntity<?> errorResponse(VerificationException e) {
        return ResponseEntity.badRequest().body(Map.of("message", e.getMessage(), "code", e.getCode()));
    }
}
