package edu.cit.mahumot.daycarelog.features.auth;

import edu.cit.mahumot.daycarelog.common.email.EmailRegistrationValidator;
import edu.cit.mahumot.daycarelog.features.users.User;
import edu.cit.mahumot.daycarelog.features.users.UserRepository;
import edu.cit.mahumot.daycarelog.common.security.JwtUtil;
import edu.cit.mahumot.daycarelog.features.verification.VerificationService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final VerificationService verificationService;
    private final EmailRegistrationValidator emailRegistrationValidator;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil,
                        VerificationService verificationService, EmailRegistrationValidator emailRegistrationValidator) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.verificationService = verificationService;
        this.emailRegistrationValidator = emailRegistrationValidator;
    }

    // Runs format -> disposable/reserved-domain -> MX record checks (in that order,
    // first failure wins) before ever touching the database. Deliberately does NOT
    // return anything revealing whether the account already existed: if it does,
    // this silently no-ops (no new row, no verification email) and the caller gets
    // back the exact same generic response as a brand-new registration - see
    // AuthController, which is why this method returns void rather than AuthResponse.
    public void register(RegisterRequest req) {
        String email = emailRegistrationValidator.validate(req.getEmail());

        if (userRepository.existsByEmail(email)) {
            return;
        }

        // Self-registration always yields STAFF. ADMIN accounts can only be created
        // by an existing admin (UserController/UserService) or the first-admin seed runner.
        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(req.getPassword()))
                .firstName(req.getFirstName())
                .lastName(req.getLastName())
                .middleName(req.getMiddleName())
                .suffix(req.getSuffix())
                .role("staff")
                .emailVerified(false)
                .build();
        user = userRepository.save(user);
        verificationService.issueVerification(user);
    }

    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }
        if (!user.getIsActive()) {
            throw new RuntimeException("This account has been deactivated. Contact an administrator.");
        }
        return buildResponse(user);
    }

    private AuthResponse buildResponse(User user) {
        boolean verified = Boolean.TRUE.equals(user.getEmailVerified());
        String token = jwtUtil.generateToken(user.getEmail(), user.getId(), user.getRole(), verified);
        AuthResponse.UserDto dto = new AuthResponse.UserDto(
                user.getId(), user.getEmail(), user.getFullName(),
                user.getFirstName(), user.getLastName(), user.getMiddleName(), user.getSuffix(),
                user.getRole(), user.getProfilePhoto(), verified);
        return new AuthResponse(token, dto);
    }
}
