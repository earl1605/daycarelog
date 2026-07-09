package edu.cit.mahumot.daycarelog.features.verification;

import edu.cit.mahumot.daycarelog.common.mail.EmailService;
import edu.cit.mahumot.daycarelog.common.security.JwtUtil;
import edu.cit.mahumot.daycarelog.features.users.User;
import edu.cit.mahumot.daycarelog.features.users.UserRepository;
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
public class VerificationService {

    static final long LINK_TTL_HOURS = 24;
    static final long CODE_TTL_MINUTES = 15;
    static final int MAX_CODE_ATTEMPTS = 5;
    static final int MAX_RESEND_PER_HOUR = 3;

    private final VerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final JwtUtil jwtUtil;
    private final SecureRandom secureRandom = new SecureRandom();

    public VerificationService(VerificationTokenRepository tokenRepository, UserRepository userRepository,
                                EmailService emailService, JwtUtil jwtUtil) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.jwtUtil = jwtUtil;
    }

    // Called right after a User row is created wherever verification is required
    // (public STAFF self-registration, Admin/Staff-created PARENT accounts).
    public void issueVerification(User user) {
        String rawToken = generateUrlSafeToken();
        String rawCode = generateSixDigitCode();
        LocalDateTime now = LocalDateTime.now();

        VerificationToken linkToken = new VerificationToken();
        linkToken.setUserId(user.getId());
        linkToken.setTokenHash(hash(rawToken));
        linkToken.setType(VerificationToken.TYPE_EMAIL_LINK);
        linkToken.setExpiresAt(now.plusHours(LINK_TTL_HOURS));
        tokenRepository.save(linkToken);

        VerificationToken codeToken = new VerificationToken();
        codeToken.setUserId(user.getId());
        codeToken.setTokenHash(hash(rawCode));
        codeToken.setType(VerificationToken.TYPE_EMAIL_CODE);
        codeToken.setExpiresAt(now.plusMinutes(CODE_TTL_MINUTES));
        tokenRepository.save(codeToken);

        emailService.sendVerificationEmail(user.getEmail(), user.getFullName(), rawToken, rawCode);
    }

    // noRollbackFor: verifyByCode deliberately saves a wrong-attempt count and then
    // throws to signal failure to the caller - a plain @Transactional would roll
    // that save back along with everything else, silently discarding every failed
    // attempt and defeating the attempt limit entirely.
    @Transactional(noRollbackFor = VerificationException.class)
    public VerifyResult verifyByToken(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new VerificationException("TOKEN_INVALID", "This verification link is invalid.");
        }
        VerificationToken vt = tokenRepository
                .findByTokenHashAndTypeAndConsumedAtIsNull(hash(rawToken), VerificationToken.TYPE_EMAIL_LINK)
                .orElseThrow(() -> new VerificationException("TOKEN_INVALID", "This verification link is invalid or has already been used."));
        if (vt.isExpired()) {
            throw new VerificationException("TOKEN_EXPIRED", "This verification link has expired. Request a new one.");
        }
        return completeVerification(vt.getUserId());
    }

    @Transactional(noRollbackFor = VerificationException.class)
    public VerifyResult verifyByCode(String email, String rawCode) {
        if (email == null || email.isBlank() || rawCode == null || rawCode.isBlank()) {
            throw new VerificationException("TOKEN_INVALID", "Invalid or expired code.");
        }
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new VerificationException("TOKEN_INVALID", "Invalid or expired code."));

        VerificationToken vt = tokenRepository
                .findFirstByUserIdAndTypeAndConsumedAtIsNullOrderByCreatedAtDesc(user.getId(), VerificationToken.TYPE_EMAIL_CODE)
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

        return completeVerification(user.getId());
    }

    // Never reveals whether the email exists or is already verified - always returns
    // normally. Rate limiting is the one observable exception (see class docs).
    public void resend(String email) {
        Optional<User> maybeUser = userRepository.findByEmail(email);
        if (maybeUser.isEmpty()) {
            return;
        }
        User user = maybeUser.get();
        if (Boolean.TRUE.equals(user.getEmailVerified())) {
            return;
        }
        LocalDateTime since = LocalDateTime.now().minusHours(1);
        long recentIssuances = tokenRepository.countByUserIdAndTypeAndCreatedAtAfter(
                user.getId(), VerificationToken.TYPE_EMAIL_CODE, since);
        if (recentIssuances >= MAX_RESEND_PER_HOUR) {
            throw new VerificationException("RATE_LIMITED", "Too many verification emails requested. Please try again later.");
        }
        issueVerification(user);
    }

    private VerifyResult completeVerification(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new VerificationException("TOKEN_INVALID", "Account not found."));
        user.setEmailVerified(true);
        user = userRepository.save(user);

        LocalDateTime now = LocalDateTime.now();
        List<VerificationToken> outstanding = tokenRepository.findByUserIdAndConsumedAtIsNull(userId);
        outstanding.forEach(t -> t.setConsumedAt(now));
        tokenRepository.saveAll(outstanding);

        String freshToken = jwtUtil.generateToken(user.getEmail(), user.getId(), user.getRole(), true);
        return new VerifyResult(user, freshToken);
    }

    // Package-private (rather than private) so unit tests can exercise the raw
    // generation logic directly.
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

    public record VerifyResult(User user, String token) {}
}
