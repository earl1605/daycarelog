package edu.cit.mahumot.daycarelog.features.passwordreset;

import edu.cit.mahumot.daycarelog.common.mail.EmailService;
import edu.cit.mahumot.daycarelog.features.users.User;
import edu.cit.mahumot.daycarelog.features.users.UserRepository;
import edu.cit.mahumot.daycarelog.features.verification.VerificationException;
import edu.cit.mahumot.daycarelog.features.verification.VerificationToken;
import edu.cit.mahumot.daycarelog.features.verification.VerificationTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PasswordResetServiceTest {

    @Mock private VerificationTokenRepository tokenRepository;
    @Mock private UserRepository userRepository;
    @Mock private EmailService emailService;
    @Mock private PasswordEncoder passwordEncoder;

    private PasswordResetService service;
    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new PasswordResetService(tokenRepository, userRepository, emailService, passwordEncoder);

        user = new User();
        user.setId(1L);
        user.setEmail("staff@example.com");
        user.setFirstName("Test");
        user.setLastName("Staff");
        user.setRole("staff");
        user.setPassword("old-encoded-hash");

        when(tokenRepository.save(any(VerificationToken.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(passwordEncoder.encode(anyString())).thenReturn("new-encoded-hash");
    }

    @Test
    void requestResetForUnknownEmailIsANoOp() {
        when(userRepository.findByEmail("nobody@example.com")).thenReturn(Optional.empty());

        service.requestReset("nobody@example.com");

        verifyNoInteractions(emailService);
        verify(tokenRepository, never()).save(any());
    }

    @Test
    void requestResetSavesOneLinkTokenAndOneCodeTokenAndSendsBothInOneEmail() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(tokenRepository.countByUserIdAndTypeAndCreatedAtAfter(
                eq(user.getId()), eq(VerificationToken.TYPE_PASSWORD_RESET_CODE), any())).thenReturn(0L);

        ArgumentCaptor<VerificationToken> captor = ArgumentCaptor.forClass(VerificationToken.class);

        service.requestReset(user.getEmail());

        verify(tokenRepository, times(2)).save(captor.capture());
        List<VerificationToken> saved = captor.getAllValues();
        assertThat(saved).extracting(VerificationToken::getType)
                .containsExactlyInAnyOrder(VerificationToken.TYPE_PASSWORD_RESET_LINK, VerificationToken.TYPE_PASSWORD_RESET_CODE);
        saved.forEach(t -> assertThat(t.getUserId()).isEqualTo(user.getId()));

        verify(emailService).sendPasswordResetEmail(eq(user.getEmail()), eq(user.getFullName()), anyString(), anyString());
    }

    @Test
    void requestResetAtHourlyLimitThrowsRateLimited() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(tokenRepository.countByUserIdAndTypeAndCreatedAtAfter(
                eq(user.getId()), eq(VerificationToken.TYPE_PASSWORD_RESET_CODE), any())).thenReturn(3L);

        assertThatThrownBy(() -> service.requestReset(user.getEmail()))
                .isInstanceOf(VerificationException.class)
                .satisfies(e -> assertThat(((VerificationException) e).getCode()).isEqualTo("RATE_LIMITED"));

        verifyNoInteractions(emailService);
    }

    @Test
    void resetByTokenWithUnknownTokenThrowsTokenInvalid() {
        when(tokenRepository.findByTokenHashAndTypeAndConsumedAtIsNull(anyString(), eq(VerificationToken.TYPE_PASSWORD_RESET_LINK)))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.resetByToken("does-not-exist", "NewPassword123"))
                .isInstanceOf(VerificationException.class)
                .satisfies(e -> assertThat(((VerificationException) e).getCode()).isEqualTo("TOKEN_INVALID"));
    }

    @Test
    void resetByTokenWithExpiredTokenThrowsTokenExpired() {
        VerificationToken expired = tokenWith(VerificationToken.TYPE_PASSWORD_RESET_LINK, LocalDateTime.now().minusMinutes(1));
        when(tokenRepository.findByTokenHashAndTypeAndConsumedAtIsNull(anyString(), eq(VerificationToken.TYPE_PASSWORD_RESET_LINK)))
                .thenReturn(Optional.of(expired));

        assertThatThrownBy(() -> service.resetByToken("raw-token", "NewPassword123"))
                .isInstanceOf(VerificationException.class)
                .satisfies(e -> assertThat(((VerificationException) e).getCode()).isEqualTo("TOKEN_EXPIRED"));
    }

    @Test
    void resetByTokenRejectsAShortPasswordBeforeTouchingTheToken() {
        assertThatThrownBy(() -> service.resetByToken("raw-token", "short"))
                .isInstanceOf(VerificationException.class)
                .satisfies(e -> assertThat(((VerificationException) e).getCode()).isEqualTo("WEAK_PASSWORD"));

        verifyNoInteractions(tokenRepository);
    }

    @Test
    void resetByTokenSuccessEncodesNewPasswordAndConsumesOnlyPasswordResetTokens() {
        VerificationToken valid = tokenWith(VerificationToken.TYPE_PASSWORD_RESET_LINK, LocalDateTime.now().plusMinutes(10));
        VerificationToken otherResetToken = tokenWith(VerificationToken.TYPE_PASSWORD_RESET_CODE, LocalDateTime.now().plusMinutes(10));
        VerificationToken unrelatedEmailToken = tokenWith(VerificationToken.TYPE_EMAIL_CODE, LocalDateTime.now().plusMinutes(10));

        when(tokenRepository.findByTokenHashAndTypeAndConsumedAtIsNull(anyString(), eq(VerificationToken.TYPE_PASSWORD_RESET_LINK)))
                .thenReturn(Optional.of(valid));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(tokenRepository.findByUserIdAndConsumedAtIsNull(user.getId()))
                .thenReturn(List.of(valid, otherResetToken, unrelatedEmailToken));

        service.resetByToken("raw-token", "NewPassword123");

        assertThat(user.getPassword()).isEqualTo("new-encoded-hash");
        verify(passwordEncoder).encode("NewPassword123");

        assertThat(valid.getConsumedAt()).isNotNull();
        assertThat(otherResetToken.getConsumedAt()).isNotNull();
        // The unrelated pending email-verification token must survive a
        // password reset - it belongs to a different feature.
        assertThat(unrelatedEmailToken.getConsumedAt()).isNull();
    }

    @Test
    void resetByCodeWithWrongCodeIncrementsAttemptsAndThrowsTokenInvalid() {
        VerificationToken codeToken = tokenWith(VerificationToken.TYPE_PASSWORD_RESET_CODE, LocalDateTime.now().plusMinutes(10));
        codeToken.setTokenHash(PasswordResetService.hash("999999"));
        codeToken.setAttempts(0);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(tokenRepository.findFirstByUserIdAndTypeAndConsumedAtIsNullOrderByCreatedAtDesc(
                user.getId(), VerificationToken.TYPE_PASSWORD_RESET_CODE)).thenReturn(Optional.of(codeToken));

        assertThatThrownBy(() -> service.resetByCode(user.getEmail(), "000000", "NewPassword123"))
                .isInstanceOf(VerificationException.class)
                .satisfies(e -> assertThat(((VerificationException) e).getCode()).isEqualTo("TOKEN_INVALID"));

        assertThat(codeToken.getAttempts()).isEqualTo(1);
        assertThat(codeToken.getConsumedAt()).isNull();
    }

    @Test
    void resetByCodeFifthWrongAttemptInvalidatesTokenAndThrowsTooManyAttempts() {
        VerificationToken codeToken = tokenWith(VerificationToken.TYPE_PASSWORD_RESET_CODE, LocalDateTime.now().plusMinutes(10));
        codeToken.setTokenHash(PasswordResetService.hash("999999"));
        codeToken.setAttempts(4);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(tokenRepository.findFirstByUserIdAndTypeAndConsumedAtIsNullOrderByCreatedAtDesc(
                user.getId(), VerificationToken.TYPE_PASSWORD_RESET_CODE)).thenReturn(Optional.of(codeToken));

        assertThatThrownBy(() -> service.resetByCode(user.getEmail(), "000000", "NewPassword123"))
                .isInstanceOf(VerificationException.class)
                .satisfies(e -> assertThat(((VerificationException) e).getCode()).isEqualTo("TOO_MANY_ATTEMPTS"));

        assertThat(codeToken.getAttempts()).isEqualTo(5);
        assertThat(codeToken.getConsumedAt()).isNotNull();
    }

    @Test
    void resetByCodeWithCorrectCodeSucceeds() {
        VerificationToken codeToken = tokenWith(VerificationToken.TYPE_PASSWORD_RESET_CODE, LocalDateTime.now().plusMinutes(10));
        codeToken.setTokenHash(PasswordResetService.hash("654321"));
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(tokenRepository.findFirstByUserIdAndTypeAndConsumedAtIsNullOrderByCreatedAtDesc(
                user.getId(), VerificationToken.TYPE_PASSWORD_RESET_CODE)).thenReturn(Optional.of(codeToken));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(tokenRepository.findByUserIdAndConsumedAtIsNull(user.getId())).thenReturn(List.of(codeToken));

        service.resetByCode(user.getEmail(), "654321", "NewPassword123");

        assertThat(user.getPassword()).isEqualTo("new-encoded-hash");
    }

    private VerificationToken tokenWith(String type, LocalDateTime expiresAt) {
        VerificationToken token = new VerificationToken();
        token.setUserId(user.getId());
        token.setType(type);
        token.setTokenHash(PasswordResetService.hash("raw-token"));
        token.setExpiresAt(expiresAt);
        token.setAttempts(0);
        return token;
    }
}
