package edu.cit.mahumot.daycarelog.features.verification;

import edu.cit.mahumot.daycarelog.common.mail.EmailService;
import edu.cit.mahumot.daycarelog.common.security.JwtUtil;
import edu.cit.mahumot.daycarelog.features.users.User;
import edu.cit.mahumot.daycarelog.features.users.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class VerificationServiceTest {

    @Mock private VerificationTokenRepository tokenRepository;
    @Mock private UserRepository userRepository;
    @Mock private EmailService emailService;
    @Mock private JwtUtil jwtUtil;

    private VerificationService service;
    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new VerificationService(tokenRepository, userRepository, emailService, jwtUtil);

        user = new User();
        user.setId(1L);
        user.setEmail("parent@example.com");
        user.setFirstName("Test");
        user.setLastName("Parent");
        user.setRole("parent");
        user.setEmailVerified(false);

        when(tokenRepository.save(any(VerificationToken.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtUtil.generateToken(anyString(), anyLong(), anyString(), anyBoolean()))
                .thenReturn("fresh.jwt.token");
    }

    // ── code/token generation ────────────────────────────────────────────

    @Test
    void sixDigitCodeIsAlwaysExactlySixDigits() {
        for (int i = 0; i < 200; i++) {
            String code = service.generateSixDigitCode();
            assertThat(code).hasSize(6);
            assertThat(code).matches("\\d{6}");
        }
    }

    @Test
    void urlSafeTokenHasNoUrlUnsafeCharactersAndIsNonTrivialLength() {
        for (int i = 0; i < 50; i++) {
            String token = service.generateUrlSafeToken();
            assertThat(token).doesNotContain("+", "/", "=");
            assertThat(token.length()).isGreaterThanOrEqualTo(40); // 32 bytes, base64url, no padding
        }
    }

    // ── hashing ──────────────────────────────────────────────────────────

    @Test
    void hashingIsDeterministicAndNeverEqualsTheRawValue() {
        String raw = "123456";
        String hash1 = VerificationService.hash(raw);
        String hash2 = VerificationService.hash(raw);
        assertThat(hash1).isEqualTo(hash2);
        assertThat(hash1).isNotEqualTo(raw);
        assertThat(hash1).hasSize(64); // SHA-256 hex digest
    }

    @Test
    void differentRawValuesHashDifferently() {
        assertThat(VerificationService.hash("111111")).isNotEqualTo(VerificationService.hash("222222"));
    }

    // ── issueVerification ────────────────────────────────────────────────

    @Test
    void issueVerificationSavesOneLinkTokenAndOneCodeTokenAndSendsBothInOneEmail() {
        ArgumentCaptor<VerificationToken> captor = ArgumentCaptor.forClass(VerificationToken.class);

        service.issueVerification(user);

        verify(tokenRepository, times(2)).save(captor.capture());
        List<VerificationToken> saved = captor.getAllValues();
        assertThat(saved).extracting(VerificationToken::getType)
                .containsExactlyInAnyOrder(VerificationToken.TYPE_EMAIL_LINK, VerificationToken.TYPE_EMAIL_CODE);
        saved.forEach(t -> assertThat(t.getUserId()).isEqualTo(user.getId()));

        VerificationToken linkToken = saved.stream().filter(t -> t.getType().equals(VerificationToken.TYPE_EMAIL_LINK)).findFirst().orElseThrow();
        VerificationToken codeToken = saved.stream().filter(t -> t.getType().equals(VerificationToken.TYPE_EMAIL_CODE)).findFirst().orElseThrow();

        // link outlives the code (24h vs 15min)
        assertThat(linkToken.getExpiresAt()).isAfter(codeToken.getExpiresAt());

        ArgumentCaptor<String> rawTokenCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> rawCodeCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService).sendVerificationEmail(eq(user.getEmail()), eq(user.getFullName()),
                rawTokenCaptor.capture(), rawCodeCaptor.capture());

        // the raw values emailed must hash to exactly what was persisted
        assertThat(VerificationService.hash(rawTokenCaptor.getValue())).isEqualTo(linkToken.getTokenHash());
        assertThat(VerificationService.hash(rawCodeCaptor.getValue())).isEqualTo(codeToken.getTokenHash());
    }

    // ── verifyByToken ────────────────────────────────────────────────────

    @Test
    void verifyByTokenWithUnknownTokenThrowsTokenInvalid() {
        when(tokenRepository.findByTokenHashAndTypeAndConsumedAtIsNull(anyString(), eq(VerificationToken.TYPE_EMAIL_LINK)))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.verifyByToken("does-not-exist"))
                .isInstanceOf(VerificationException.class)
                .satisfies(e -> assertThat(((VerificationException) e).getCode()).isEqualTo("TOKEN_INVALID"));
    }

    @Test
    void verifyByTokenWithExpiredTokenThrowsTokenExpired() {
        VerificationToken expired = tokenWith(VerificationToken.TYPE_EMAIL_LINK, LocalDateTime.now().minusMinutes(1));
        when(tokenRepository.findByTokenHashAndTypeAndConsumedAtIsNull(anyString(), eq(VerificationToken.TYPE_EMAIL_LINK)))
                .thenReturn(Optional.of(expired));

        assertThatThrownBy(() -> service.verifyByToken("raw-token"))
                .isInstanceOf(VerificationException.class)
                .satisfies(e -> assertThat(((VerificationException) e).getCode()).isEqualTo("TOKEN_EXPIRED"));
    }

    @Test
    void verifyByTokenSuccessMarksUserVerifiedAndConsumesAllOutstandingTokens() {
        VerificationToken valid = tokenWith(VerificationToken.TYPE_EMAIL_LINK, LocalDateTime.now().plusHours(1));
        VerificationToken otherOutstanding = tokenWith(VerificationToken.TYPE_EMAIL_CODE, LocalDateTime.now().plusMinutes(10));

        when(tokenRepository.findByTokenHashAndTypeAndConsumedAtIsNull(anyString(), eq(VerificationToken.TYPE_EMAIL_LINK)))
                .thenReturn(Optional.of(valid));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(tokenRepository.findByUserIdAndConsumedAtIsNull(user.getId()))
                .thenReturn(List.of(valid, otherOutstanding));

        VerificationService.VerifyResult result = service.verifyByToken("raw-token");

        assertThat(user.getEmailVerified()).isTrue();
        assertThat(valid.getConsumedAt()).isNotNull();
        assertThat(otherOutstanding.getConsumedAt()).isNotNull();
        assertThat(result.token()).isEqualTo("fresh.jwt.token");
        verify(jwtUtil).generateToken(user.getEmail(), user.getId(), user.getRole(), true);
    }

    // ── verifyByCode ─────────────────────────────────────────────────────

    @Test
    void verifyByCodeWithUnknownEmailThrowsTokenInvalid() {
        when(userRepository.findByEmail("nobody@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.verifyByCode("nobody@example.com", "123456"))
                .isInstanceOf(VerificationException.class)
                .satisfies(e -> assertThat(((VerificationException) e).getCode()).isEqualTo("TOKEN_INVALID"));
    }

    @Test
    void verifyByCodeWithExpiredCodeThrowsTokenExpired() {
        VerificationToken expired = tokenWith(VerificationToken.TYPE_EMAIL_CODE, LocalDateTime.now().minusMinutes(1));
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(tokenRepository.findFirstByUserIdAndTypeAndConsumedAtIsNullOrderByCreatedAtDesc(user.getId(), VerificationToken.TYPE_EMAIL_CODE))
                .thenReturn(Optional.of(expired));

        assertThatThrownBy(() -> service.verifyByCode(user.getEmail(), "123456"))
                .isInstanceOf(VerificationException.class)
                .satisfies(e -> assertThat(((VerificationException) e).getCode()).isEqualTo("TOKEN_EXPIRED"));
    }

    @Test
    void verifyByCodeWithWrongCodeIncrementsAttemptsAndThrowsTokenInvalid() {
        VerificationToken codeToken = tokenWith(VerificationToken.TYPE_EMAIL_CODE, LocalDateTime.now().plusMinutes(10));
        codeToken.setTokenHash(VerificationService.hash("999999"));
        codeToken.setAttempts(0);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(tokenRepository.findFirstByUserIdAndTypeAndConsumedAtIsNullOrderByCreatedAtDesc(user.getId(), VerificationToken.TYPE_EMAIL_CODE))
                .thenReturn(Optional.of(codeToken));

        assertThatThrownBy(() -> service.verifyByCode(user.getEmail(), "000000"))
                .isInstanceOf(VerificationException.class)
                .satisfies(e -> assertThat(((VerificationException) e).getCode()).isEqualTo("TOKEN_INVALID"));

        assertThat(codeToken.getAttempts()).isEqualTo(1);
        assertThat(codeToken.getConsumedAt()).isNull();
    }

    @Test
    void verifyByCodeFifthWrongAttemptInvalidatesTokenAndThrowsTooManyAttempts() {
        VerificationToken codeToken = tokenWith(VerificationToken.TYPE_EMAIL_CODE, LocalDateTime.now().plusMinutes(10));
        codeToken.setTokenHash(VerificationService.hash("999999"));
        codeToken.setAttempts(4); // one more wrong guess hits the max of 5
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(tokenRepository.findFirstByUserIdAndTypeAndConsumedAtIsNullOrderByCreatedAtDesc(user.getId(), VerificationToken.TYPE_EMAIL_CODE))
                .thenReturn(Optional.of(codeToken));

        assertThatThrownBy(() -> service.verifyByCode(user.getEmail(), "000000"))
                .isInstanceOf(VerificationException.class)
                .satisfies(e -> assertThat(((VerificationException) e).getCode()).isEqualTo("TOO_MANY_ATTEMPTS"));

        assertThat(codeToken.getAttempts()).isEqualTo(5);
        assertThat(codeToken.getConsumedAt()).isNotNull(); // invalidated, can't be retried
    }

    @Test
    void verifyByCodeWithCorrectCodeSucceeds() {
        VerificationToken codeToken = tokenWith(VerificationToken.TYPE_EMAIL_CODE, LocalDateTime.now().plusMinutes(10));
        codeToken.setTokenHash(VerificationService.hash("654321"));
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(tokenRepository.findFirstByUserIdAndTypeAndConsumedAtIsNullOrderByCreatedAtDesc(user.getId(), VerificationToken.TYPE_EMAIL_CODE))
                .thenReturn(Optional.of(codeToken));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(tokenRepository.findByUserIdAndConsumedAtIsNull(user.getId())).thenReturn(List.of(codeToken));

        VerificationService.VerifyResult result = service.verifyByCode(user.getEmail(), "654321");

        assertThat(user.getEmailVerified()).isTrue();
        assertThat(result.token()).isEqualTo("fresh.jwt.token");
    }

    // ── resend rate limiting ─────────────────────────────────────────────

    @Test
    void resendForUnknownEmailIsANoOp() {
        when(userRepository.findByEmail("nobody@example.com")).thenReturn(Optional.empty());

        service.resend("nobody@example.com"); // must not throw

        verifyNoInteractions(emailService);
        verify(tokenRepository, never()).save(any());
    }

    @Test
    void resendForAlreadyVerifiedAccountIsANoOp() {
        user.setEmailVerified(true);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        service.resend(user.getEmail());

        verifyNoInteractions(emailService);
        verify(tokenRepository, never()).save(any());
    }

    @Test
    void resendUnderTheHourlyLimitReissuesVerification() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(tokenRepository.countByUserIdAndTypeAndCreatedAtAfter(eq(user.getId()), eq(VerificationToken.TYPE_EMAIL_CODE), any()))
                .thenReturn(2L); // under MAX_RESEND_PER_HOUR (3)

        service.resend(user.getEmail());

        verify(tokenRepository, times(2)).save(any(VerificationToken.class));
        verify(emailService).sendVerificationEmail(eq(user.getEmail()), any(), any(), any());
    }

    @Test
    void resendAtTheHourlyLimitThrowsRateLimited() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(tokenRepository.countByUserIdAndTypeAndCreatedAtAfter(eq(user.getId()), eq(VerificationToken.TYPE_EMAIL_CODE), any()))
                .thenReturn(3L); // at MAX_RESEND_PER_HOUR

        assertThatThrownBy(() -> service.resend(user.getEmail()))
                .isInstanceOf(VerificationException.class)
                .satisfies(e -> assertThat(((VerificationException) e).getCode()).isEqualTo("RATE_LIMITED"));

        verifyNoInteractions(emailService);
    }

    private VerificationToken tokenWith(String type, LocalDateTime expiresAt) {
        VerificationToken token = new VerificationToken();
        token.setUserId(user.getId());
        token.setType(type);
        token.setTokenHash(VerificationService.hash("raw-token"));
        token.setExpiresAt(expiresAt);
        token.setAttempts(0);
        return token;
    }
}
