package edu.cit.mahumot.daycarelog.features.passwordreset;

import edu.cit.mahumot.daycarelog.common.mail.EmailService;
import edu.cit.mahumot.daycarelog.features.users.User;
import edu.cit.mahumot.daycarelog.features.users.UserRepository;
import edu.cit.mahumot.daycarelog.features.verification.VerificationException;
import edu.cit.mahumot.daycarelog.features.verification.VerificationToken;
import edu.cit.mahumot.daycarelog.features.verification.VerificationTokenRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Service
public class PasswordResetService {

    // Shorter-lived than email verification (VerificationService.LINK_TTL_HOURS) -
    // a password reset link is more sensitive, so it stays valid for less time.
    static final long LINK_TTL_MINUTES = 30;
    static final long CODE_TTL_MINUTES = 15;
    static final int MAX_CODE_ATTEMPTS = 5;
    static final int MAX_REQUESTS_PER_HOUR = 3;

    private final VerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();

    public PasswordResetService(VerificationTokenRepository tokenRepository, UserRepository userRepository,
                                 EmailService emailService, PasswordEncoder passwordEncoder) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    public void requestReset(String email) {
        if (email == null || email.isBlank()) {
            return;
        }
        Optional<User> maybeUser = userRepository.findByEmail(email);
        if (maybeUser.isEmpty()) {
            // No enumeration: silently no-op for an unknown address, same as
            // VerificationService.resend().
            return;
        }
        User user = maybeUser.get();

        LocalDateTime since = LocalDateTime.now().minusHours(1);
        long recentRequests = tokenRepository.countByUserIdAndTypeAndCreatedAtAfter(
                user.getId(), VerificationToken.TYPE_PASSWORD_RESET_CODE, since);
        if (recentRequests >= MAX_REQUESTS_PER_HOUR) {
            throw new VerificationException("RATE_LIMITED", "Too many password reset requests. Please try again later.");
        }

        issueReset(user);
    }

    private void issueReset(User user) {
        String rawToken = generateUrlSafeToken();
        String rawCode = generateSixDigitCode();
        LocalDateTime now = LocalDateTime.now();

        VerificationToken linkToken = new VerificationToken();
        linkToken.setUserId(user.getId());
        linkToken.setTokenHash(hash(rawToken));
        linkToken.setType(VerificationToken.TYPE_PASSWORD_RESET_LINK);
        linkToken.setExpiresAt(now.plusMinutes(LINK_TTL_MINUTES));
        tokenRepository.save(linkToken);

        VerificationToken codeToken = new VerificationToken();
        codeToken.setUserId(user.getId());
        codeToken.setTokenHash(hash(rawCode));
        codeToken.setType(VerificationToken.TYPE_PASSWORD_RESET_CODE);
        codeToken.setExpiresAt(now.plusMinutes(CODE_TTL_MINUTES));
        tokenRepository.save(codeToken);

        emailService.sendPasswordResetEmail(user.getEmail(), user.getFullName(), rawToken, rawCode);
    }

    @Transactional(noRollbackFor = VerificationException.class)
    public void resetByToken(String rawToken, String newPassword) {
        validateNewPassword(newPassword);
        if (rawToken == null || rawToken.isBlank()) {
            throw new VerificationException("TOKEN_INVALID", "This reset link is invalid.");
        }
        VerificationToken vt = tokenRepository
                .findByTokenHashAndTypeAndConsumedAtIsNull(hash(rawToken), VerificationToken.TYPE_PASSWORD_RESET_LINK)
                .orElseThrow(() -> new VerificationException("TOKEN_INVALID", "This reset link is invalid or has already been used."));
        if (vt.isExpired()) {
            throw new VerificationException("TOKEN_EXPIRED", "This reset link has expired. Request a new one.");
        }
        applyNewPassword(vt.getUserId(), newPassword);
    }

    @Transactional(noRollbackFor = VerificationException.class)
    public void resetByCode(String email, String rawCode, String newPassword) {
        validateNewPassword(newPassword);
        if (email == null || email.isBlank() || rawCode == null || rawCode.isBlank()) {
            throw new VerificationException("TOKEN_INVALID", "Invalid or expired code.");
        }
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new VerificationException("TOKEN_INVALID", "Invalid or expired code."));

        VerificationToken vt = tokenRepository
                .findFirstByUserIdAndTypeAndConsumedAtIsNullOrderByCreatedAtDesc(user.getId(), VerificationToken.TYPE_PASSWORD_RESET_CODE)
                .orElseThrow(() -> new VerificationException("TOKEN_INVALID", "Invalid or expired code."));

        if (vt.isExpired()) {
            throw new VerificationException("TOKEN_EXPIRED", "This code has expired. Request a new one.");
        }

        if (!hash(rawCode).equals(vt.getTokenHash())) {
            vt.setAttempts(vt.getAttempts() + 1);
            if (vt.getAttempts() >= MAX_CODE_ATTEMPTS) {
                vt.setConsumedAt(LocalDateTime.now());
                tokenRepository.save(vt);
                throw new VerificationException("TOO_MANY_ATTEMPTS", "Too many incorrect attempts. Request a new code.");
            }
            tokenRepository.save(vt);
            throw new VerificationException("TOKEN_INVALID", "Incorrect code.");
        }

        applyNewPassword(user.getId(), newPassword);
    }

    private void validateNewPassword(String newPassword) {
        if (newPassword == null || newPassword.length() < 8) {
            throw new VerificationException("WEAK_PASSWORD", "Password must be at least 8 characters");
        }
    }

    private void applyNewPassword(Long userId, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new VerificationException("TOKEN_INVALID", "Account not found."));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Only consume this user's outstanding password-reset tokens - the
        // token table is shared with email verification (VerificationToken),
        // and an unrelated pending EMAIL_LINK/EMAIL_CODE must survive a
        // password reset rather than being silently invalidated alongside it.
        LocalDateTime now = LocalDateTime.now();
        List<VerificationToken> outstanding = tokenRepository.findByUserIdAndConsumedAtIsNull(userId).stream()
                .filter(t -> VerificationToken.TYPE_PASSWORD_RESET_LINK.equals(t.getType())
                        || VerificationToken.TYPE_PASSWORD_RESET_CODE.equals(t.getType()))
                .toList();
        outstanding.forEach(t -> t.setConsumedAt(now));
        tokenRepository.saveAll(outstanding);
    }

    String generateUrlSafeToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    String generateSixDigitCode() {
        int code = secureRandom.nextInt(1_000_000);
        return String.format("%06d", code);
    }

    static String hash(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(hashed.length * 2);
            for (byte b : hashed) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
