package edu.cit.mahumot.daycarelog.features.passwordreset;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.cit.mahumot.daycarelog.common.mail.EmailService;
import edu.cit.mahumot.daycarelog.features.users.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PasswordResetEndpointsTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @MockitoSpyBean private EmailService emailService;

    private static final String EMAIL = "reset-test@test.daycarelog";
    private static final String PASSWORD = "OriginalPass123";

    @BeforeEach
    void setUp() throws Exception {
        userRepository.findByEmail(EMAIL).ifPresent(userRepository::delete);
        doCallRealMethod().when(emailService)
                .sendPasswordResetEmail(anyString(), any(), anyString(), anyString());
        doCallRealMethod().when(emailService)
                .sendVerificationEmail(anyString(), any(), anyString(), anyString());

        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", EMAIL, "password", PASSWORD,
                                "firstName", "Reset", "lastName", "Test"))))
                .andExpect(status().isCreated());
    }

    private String[] requestResetAndCaptureRawTokenAndCode() throws Exception {
        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("email", EMAIL))))
                .andExpect(status().isOk());

        ArgumentCaptor<String> tokenCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService).sendPasswordResetEmail(eq(EMAIL), any(), tokenCaptor.capture(), codeCaptor.capture());
        return new String[]{tokenCaptor.getValue(), codeCaptor.getValue()};
    }

    @Test
    void forgotPasswordForUnknownEmailReturnsTheSameGenericMessage() throws Exception {
        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("email", "no-such-account@example.com"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void resetByLinkTokenSucceedsAndNewPasswordWorksAtLogin() throws Exception {
        String[] tokenAndCode = requestResetAndCaptureRawTokenAndCode();
        String rawToken = tokenAndCode[0];

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("token", rawToken, "newPassword", "BrandNewPass123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists());

        // Old password no longer works.
        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("email", EMAIL, "password", PASSWORD))))
                .andExpect(status().isBadRequest());

        // New password logs in successfully.
        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("email", EMAIL, "password", "BrandNewPass123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    void reusingAnAlreadyConsumedResetLinkFails() throws Exception {
        String[] tokenAndCode = requestResetAndCaptureRawTokenAndCode();
        String rawToken = tokenAndCode[0];

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("token", rawToken, "newPassword", "BrandNewPass123"))))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("token", rawToken, "newPassword", "AnotherPass456"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("TOKEN_INVALID"));
    }

    @Test
    void resetByCodeSucceeds() throws Exception {
        String[] tokenAndCode = requestResetAndCaptureRawTokenAndCode();
        String rawCode = tokenAndCode[1];

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(
                                Map.of("email", EMAIL, "code", rawCode, "newPassword", "BrandNewPass123"))))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("email", EMAIL, "password", "BrandNewPass123"))))
                .andExpect(status().isOk());
    }

    @Test
    void resetWithAShortPasswordIsRejected() throws Exception {
        String[] tokenAndCode = requestResetAndCaptureRawTokenAndCode();
        String rawToken = tokenAndCode[0];

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("token", rawToken, "newPassword", "short"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("WEAK_PASSWORD"));

        // Original password still works - the weak new password was never applied.
        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("email", EMAIL, "password", PASSWORD))))
                .andExpect(status().isOk());
    }

    @Test
    void resettingWithAGarbageTokenFails() throws Exception {
        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("token", "not-a-real-token", "newPassword", "BrandNewPass123"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("TOKEN_INVALID"));
    }

    @Test
    void resettingPasswordDoesNotConsumeAPendingEmailVerificationToken() throws Exception {
        // The account from setUp() is still unverified (registration issues an
        // EMAIL_LINK/EMAIL_CODE pair). Requesting and completing a password
        // reset must not silently invalidate that pending verification.
        String[] resetTokenAndCode = requestResetAndCaptureRawTokenAndCode();
        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(
                                Map.of("token", resetTokenAndCode[0], "newPassword", "BrandNewPass123"))))
                .andExpect(status().isOk());

        assertThat(userRepository.findByEmail(EMAIL).orElseThrow().getEmailVerified()).isFalse();

        ArgumentCaptor<String> emailVerifyTokenCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService).sendVerificationEmail(eq(EMAIL), any(), emailVerifyTokenCaptor.capture(), anyString());

        mockMvc.perform(post("/api/auth/verify-email")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("token", emailVerifyTokenCaptor.getValue()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.emailVerified").value(true));
    }
}
